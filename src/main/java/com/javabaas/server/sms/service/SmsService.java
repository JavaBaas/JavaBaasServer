package com.javabaas.server.sms.service;

import com.javabaas.server.config.SmsConfig;
import com.javabaas.server.sms.entity.SmsSendResult;
import com.javabaas.server.sms.handler.ISmsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务
 * Created by Codi on 2017/6/27.
 */
@Service
public class SmsService {

    private static final String SMS_CODE_NAME = "_SMS_CODE";
    private ISmsHandler smsHandler;
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

    public SmsSendResult sendSms(String appId, String phoneNumber, String signName, String templateId, Map<String, String> params) {
        //请求频率限制
        rateLimiter.rate(appId, phoneNumber, signName, templateId, params);
        //发送
        return smsHandler.sendSms(phoneNumber, signName, templateId, params);
    }

    /**
     * 发送手机验证码
     *
     * @param phoneNumber 电话号码
     * @param ttl         失效时间(秒)
     */
    public SmsSendResult sendSmsCode(String appId, String phoneNumber, long ttl) {
        //生成六位随机数字验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        //记录验证码
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(getKey(appId, phoneNumber), code, ttl, TimeUnit.SECONDS);
        //发送短信
        Map<String, String> params = new HashMap<>();
        //短信验证码参数固定为code
        params.put("code", code);
        return sendSms(appId, phoneNumber, smsConfig.getSignName(), smsConfig.getSmsCodeTemplateId(), params);
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
