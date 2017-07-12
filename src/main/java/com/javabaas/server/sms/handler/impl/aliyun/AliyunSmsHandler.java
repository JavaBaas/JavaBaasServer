package com.javabaas.server.sms.handler.impl.aliyun;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.sms.entity.SmsSendResult;
import com.javabaas.server.sms.handler.ISmsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AliyunSmsHandler implements ISmsHandler {

    @Autowired
    private AppConfigService appConfigService;
    @Autowired
    private JSONUtil jsonUtil;

    @Override
    public SmsSendResult sendSms(String appId, String id, String phone, String signName, String templateId, BaasObject params) {
        try {
            //初始化acsClient
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey(appId), secret(appId));
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Dysmsapi", "dysmsapi.aliyuncs.com");
            IAcsClient acsClient = new DefaultAcsClient(profile);
            //组装请求对象
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode("SMS_73090044");
            params.put("product", "硬壳科技");
            request.setTemplateParam(jsonUtil.writeValueAsString(params));
            //流水号
            request.setOutId(id);

            //此处可能会抛出异常，注意catch
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (ClientException e) {
            //发送失败
            e.printStackTrace();
        }
        return null;
    }

    private String accessKey(String appId) {
        return appConfigService.getString(appId, AppConfigEnum.SMS_HANDLER_ALIYUN_KEY);
    }

    private String secret(String appId) {
        return appConfigService.getString(appId, AppConfigEnum.SMS_HANDLER_ALIYUN_SECRET);
    }

}
