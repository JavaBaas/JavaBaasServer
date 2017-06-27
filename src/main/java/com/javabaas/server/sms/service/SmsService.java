package com.javabaas.server.sms.service;

import com.javabaas.server.sms.entity.SmsSendResult;
import com.javabaas.server.sms.handler.ISmsHandler;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 短信服务
 * Created by Codi on 2017/6/27.
 */
@Service
public class SmsService {

    private ISmsHandler smsHandler;

    public SmsSendResult sendSms(String phoneNumber, String signName, String templateCode, Map<String, String> params) {
        return smsHandler.sendSms(phoneNumber, signName, templateCode, params);
    }

}
