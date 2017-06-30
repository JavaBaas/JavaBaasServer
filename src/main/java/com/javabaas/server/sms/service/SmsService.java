package com.javabaas.server.sms.service;

import com.javabaas.server.config.SmsConfig;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.sms.entity.SmsLog;
import com.javabaas.server.sms.entity.SmsSendResult;
import com.javabaas.server.sms.entity.SmsSendResultCode;
import com.javabaas.server.sms.entity.SmsSendState;
import com.javabaas.server.sms.handler.ISmsHandler;
import com.javabaas.server.sms.handler.impl.MockSmsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务
 * Created by Codi on 2017/6/27.
 */
@Service
public class SmsService {

    public static final String SMS_LOG_CLASS_NAME = "_SmsLog";
    private static final String SMS_CODE_NAME = "_SMS_CODE";
    @Resource(type = MockSmsHandler.class)
    private ISmsHandler smsHandler;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private SmsConfig smsConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SmsRateLimiter rateLimiter;

    public ISmsHandler getSmsHandler() {
        return smsHandler;
    }

    public void setSmsHandler(ISmsHandler smsHandler) {
        this.smsHandler = smsHandler;
    }

    public SmsSendResult sendSms(String appId, String plat, String phoneNumber, String templateId, BaasObject params) {
        //请求频率限制
        rateLimiter.rate(appId, phoneNumber, templateId, params);
        //获取短信签名
        String signName = smsConfig.getSignName();
        //记录发送日志
        SmsLog smsLog = new SmsLog();
        smsLog.setPhoneNumber(phoneNumber);
        smsLog.setSignName(signName);
        smsLog.setTemplateId(templateId);
        smsLog.setParams(params);
        smsLog.setState(SmsSendState.WAIT.getCode());
        smsLog = new SmsLog(objectService.insert(appId, plat, SMS_LOG_CLASS_NAME, smsLog, null, true));
        //发送
        SmsSendResult smsSendResult = smsHandler.sendSms(smsLog.getId(), phoneNumber, signName, templateId, params);
        if (smsSendResult.getCode() == SmsSendResultCode.SUCCESS.getCode()) {
            //发送成功
            smsLog.setState(SmsSendState.SUCCESS.getCode());
            objectService.update(appId, plat, SMS_LOG_CLASS_NAME, smsLog.getId(), smsLog, null, true);
        } else {
            //发送失败
            smsLog.setState(SmsSendState.FAIL.getCode());
            objectService.update(appId, plat, SMS_LOG_CLASS_NAME, smsLog.getId(), smsLog, null, true);
        }
        return smsSendResult;
    }

    /**
     * 发送手机验证码
     *
     * @param phoneNumber 电话号码
     * @param ttl         失效时间(秒)
     */
    public SmsSendResult sendSmsCode(String appId, String plat, String phoneNumber, long ttl) {
        //生成六位随机数字验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        //记录验证码
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(getKey(appId, phoneNumber), code, ttl, TimeUnit.SECONDS);
        //发送短信
        BaasObject params = new BaasObject();
        //短信验证码参数固定为code
        params.put("code", code);
        return sendSms(appId, plat, phoneNumber, smsConfig.getSmsCodeTemplateId(), params);
    }

    /**
     * 验证手机验证码
     *
     * @param phoneNumber 电话号码
     * @param code        验证码
     */
    public boolean verifySmsCode(String appId, String phoneNumber, String code) {
        //获取已缓存的手机验证码
        String key = getKey(appId, phoneNumber);
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String rightCode = ops.get(key);
        if (StringUtils.isEmpty(rightCode)) {
            //验证码不存在
            return false;
        } else {
            //验证码存在
            if (code.equals(rightCode)) {
                //验证成功 删除缓存中的验证码
                redisTemplate.delete(key);
                return true;
            } else {
                //尝试次数限制
                Long times = ops.increment(key + "_times", 1);
                if (times > smsConfig.getTryErrorLimit()) {
                    //超过尝试次数限制 删除缓存中的验证码
                    redisTemplate.delete(key);
                }
                return false;
            }
        }
    }

    private String getKey(String appId, String phoneNumber) {
        return "App_" + appId + SMS_CODE_NAME + "_" + phoneNumber;
    }

}
