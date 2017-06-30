package com.javabaas.server.sms.service;

import com.javabaas.server.config.SmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信请求频度限制
 * Created by Codi on 2017/6/29.
 */
@Component
public class SmsRateLimiter {

    @Autowired
    private SmsConfig smsConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;

    void rate(String appId, String phoneNumber, String signName, String templateId, Map<String, String> params) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = "App_" + appId + "_SMS__LIMIT_" + phoneNumber;
    }

}
