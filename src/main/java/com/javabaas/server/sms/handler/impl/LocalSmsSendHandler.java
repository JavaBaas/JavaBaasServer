package com.javabaas.server.sms.handler.impl;

import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.sms.handler.ISmsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Codi on 2017/6/30.
 */
public class LocalSmsSendHandler implements ISmsHandler {

    @Autowired
    private RestTemplate rest;

    @Override
    public SimpleResult sendSms(String appId, String id, String phone, String signName, String templateId, BaasObject params) {
        return null;
    }

}
