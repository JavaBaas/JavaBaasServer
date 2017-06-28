package com.javabaas.server.sms.handler.impl;

import com.javabaas.server.sms.entity.SmsSendResult;
import com.javabaas.server.sms.handler.ISmsHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于测试的短信发送器
 * Created by Codi on 2017/6/28.
 */
@Component
public class MockSmsHandler implements ISmsHandler {

    private Map<String, String> map = new HashMap<>();

    public String getSms(String phoneNumber) {
        return map.get(phoneNumber);
    }

    @Override
    public SmsSendResult sendSms(String phoneNumber, String signName, String templateId, Map<String, String> params) {
        StringBuilder sms = new StringBuilder();
        params.forEach((k, v) -> sms.append(v));
        map.put(phoneNumber, sms.toString());
        return SmsSendResult.success();
    }

}
