package com.javabaas.server.sms.service;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.sms.entity.SmsLog;
import com.javabaas.server.sms.entity.SmsSendState;
import com.javabaas.server.sms.handler.ISmsHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务
 * Created by Codi on 2017/6/27.
 */
@Service
public class SmsService {

    public static final String SMS_LOG_CLASS_NAME = "_SmsLog";
    private static final String SMS_CODE_NAME = "_SMS_CODE";
    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private Map<String, ISmsHandler> handlers;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private AppConfigService appConfigService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SmsRateLimiter rateLimiter;

    public SimpleResult sendSms(String appId, String plat, String phone, String templateId, BaasObject params) {
        //请求频率限制
        rateLimiter.rate(appId, phone, templateId, params);
        //获取短信签名
        String signName = appConfigService.getString(appId, AppConfigEnum.SMS_SIGN_NAME);
        if (StringUtils.isEmpty(signName)) {
            log.warn(SimpleCode.SMS_NO_SIGN_NAME.getMessage() + " phone:" + phone + " template:" + templateId);
            throw new SimpleError(SimpleCode.SMS_NO_SIGN_NAME);
        }
        //记录发送日志
        SmsLog smsLog = new SmsLog();
        smsLog.setPhone(phone);
        smsLog.setSignName(signName);
        smsLog.setTemplateId(templateId);
        smsLog.setParams(params);
        smsLog.setState(SmsSendState.WAIT.getCode());
        smsLog = new SmsLog(objectService.insert(appId, plat, SMS_LOG_CLASS_NAME, smsLog, true, null, true));
        //发送
        SimpleResult sendResult = getSmsHandler(appId).sendSms(appId, smsLog.getId(), phone, signName, templateId, params);
        if (sendResult.getCode() != SimpleCode.SUCCESS.getCode()) {
            //发送失败
            smsLog.setState(SmsSendState.FAIL.getCode());
            objectService.update(appId, plat, SMS_LOG_CLASS_NAME, smsLog.getId(), smsLog, null, true);
            //抛出发送失败异常
            throw new SimpleError(sendResult.getCode(), sendResult.getMessage());
        } else {
            //发送成功
            smsLog.setState(SmsSendState.SUCCESS.getCode());
            objectService.update(appId, plat, SMS_LOG_CLASS_NAME, smsLog.getId(), smsLog, null, true);
        }
        return sendResult;
    }

    /**
     * 发送手机验证码
     *
     * @param phone  电话号码
     * @param ttl    失效时间(秒)
     * @param params
     */
    public SimpleResult sendSmsCode(String appId, String plat, String templateId, String phone, long ttl, BaasObject params) {
        if (StringUtils.isEmpty(templateId)) {
            templateId = getSmsCodeTemplateId(appId);
        }
        //生成六位随机数字验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        //记录验证码
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(getKey(appId, templateId, phone), code, ttl, TimeUnit.SECONDS);
        //发送短信
        if (params == null) {
            params = new BaasObject();
        }
        //短信验证码参数固定为code
        params.put("code", code);
        //删除尝试次数
        String key = getKey(appId, templateId, phone);
        redisTemplate.delete(key + "_times");
        return sendSms(appId, plat, phone, templateId, params);
    }

    public SimpleResult sendSmsCode(String appId, String plat, String phone, long ttl, BaasObject params) {
        return sendSmsCode(appId, plat, getSmsCodeTemplateId(appId), phone, ttl, params);
    }

    /**
     * 验证手机验证码
     *
     * @param phone 电话号码
     * @param code  验证码
     */
    public boolean verifySmsCode(String appId, String templateId, String phone, String code) {
        // 查看是否为万能验证码
        String superCode = appConfigService.getString(appId, AppConfigEnum.SMS_REGISTER_SUPER_CODE);
        if (superCode != null && superCode.equals(code)) {
            return true;
        }
        //获取已缓存的手机验证码
        String key = getKey(appId, templateId, phone);
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
                if (times > appConfigService.getLong(appId, AppConfigEnum.SMS_TRY_LIMIT)) {
                    //超过尝试次数限制 删除缓存中的验证码
                    redisTemplate.delete(key);
                    SimpleError.e(SimpleCode.SMS_WRONG_TRY_LIMIT);
                }
                return false;
            }
        }
    }

    public boolean verifySmsCode(String appId, String phone, String code) {
        return verifySmsCode(appId, getSmsCodeTemplateId(appId), phone, code);
    }

    private String getSmsCodeTemplateId(String appId) {
        String templateId = appConfigService.getString(appId, AppConfigEnum.SMS_CODE_TEMPLATE_ID);
        if (StringUtils.isEmpty(templateId)) {
            throw new SimpleError(SimpleCode.SMS_CODE_TEMPLATE);
        }
        return templateId;
    }

    /**
     * 选择短信处理器
     */
    private ISmsHandler getSmsHandler(String appId) {
        String handlerName = appConfigService.getString(appId, AppConfigEnum.SMS_HANDLER);
        if (StringUtils.isEmpty(handlerName)) {
            //短信处理器未定义
            SimpleError.e(SimpleCode.SMS_HANDLER_NOT_DEFINE);
        }
        ISmsHandler handler = handlers.get(handlerName);
        if (handler == null) {
            //短信处理器未找到
            SimpleError.e(SimpleCode.SMS_HANDLER_NOT_FOUND);
        }
        return handler;
    }

    private String getKey(String appId, String templateId, String phone) {
        return "App_" + appId + SMS_CODE_NAME + "_" + templateId + "_" + phone;
    }

}
