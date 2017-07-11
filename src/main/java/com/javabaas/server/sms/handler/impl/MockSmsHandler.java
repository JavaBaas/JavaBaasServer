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

    public String getSms(String phone) {
        return map.get(phone);
    }

    @Override
    public SmsSendResult sendSms(String appId, String id, String phone, String signName, String templateId, BaasObject params) {
        StringBuilder sms = new StringBuilder();
        if (params != null) {
            params.forEach((k, v) -> sms.append(v));
        }
        map.put(phone, sms.toString());
        log.info("Mock短信 phone:" + phone + " sms:" + sms);
        return SmsSendResult.success();
    }

}
