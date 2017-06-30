package com.javabaas.server.sms.handler;

import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.sms.entity.SmsSendResult;

/**
 * Created by Codi on 2017/6/26.
 */
public interface ISmsHandler {

    /**
     * 发送短信
     *
     * @param id          流水号
     * @param phoneNumber 目标电话号码
     * @param signName    短信签名
     * @param templateId  模版编号
     * @param params      参数
     * @return 发送结果
     */
    SmsSendResult sendSms(String id, String phoneNumber, String signName, String templateId, BaasObject params);

}
