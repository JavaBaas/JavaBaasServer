package com.javabaas.server.sms.handler.impl;

import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.sms.entity.SmsSendResult;
import com.javabaas.server.sms.handler.ISmsHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于测试的短信发送器
 * Created by Codi on 2017/6/28.
 */
@Component
public class MockSmsHandler implements ISmsHandler {

    private Log log = LogFactory.getLog(getClass());

    private Map<String, String> map = new HashMap<>();

    public String getSms(String phoneNumber) {
        return map.get(phoneNumber);
    }

    @Override
    public SmsSendResult sendSms(String id, String phoneNumber, String signName, String templateId, BaasObject params) {
        StringBuilder sms = new StringBuilder();
        if (params != null) {
            params.forEach((k, v) -> sms.append(v));
        }
        map.put(phoneNumber, sms.toString());
        log.info("Mock短信 phoneNumber:" + phoneNumber + " sms:" + sms);
        return SmsSendResult.success();
    }

}
