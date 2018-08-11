package com.javabaas.server.sms.handler.impl.aliyun;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.sms.handler.ISmsHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("aliyun")
public class AliyunSmsHandler implements ISmsHandler {

    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private AppConfigService appConfigService;
    @Autowired
    private JSONUtil jsonUtil;

    @Override
    public SimpleResult sendSms(String appId, String id, String phone, String signName, String templateId, BaasObject params) {
        try {
            //初始化acsClient
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey(appId), secret(appId));
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Dysmsapi", "dysmsapi.aliyuncs.com");
            IAcsClient acsClient = new DefaultAcsClient(profile);
            //组装请求对象
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateId);
            request.setTemplateParam(jsonUtil.writeValueAsString(params));
            //流水号
            request.setOutId(id);
            SendSmsResponse response = acsClient.getAcsResponse(request);
            if (response.getCode().equals("OK")) {
                //发送成功
                return SimpleResult.success();
            } else {
                //发送失败
                log.warn("阿里云短信发送失败 code:" + response.getCode() + " message:" + response.getMessage());
                switch (response.getCode()) {
                    case "isv.AMOUNT_NOT_ENOUGH":
                        return SimpleResult.error(SimpleCode.SMS_AMOUNT_NOT_ENOUGH);
                    case "isv.MOBILE_NUMBER_ILLEGAL":
                        return SimpleResult.error(SimpleCode.SMS_ILLEGAL_PHONE_NUMBER);
                    case "isv.INVALID_PARAMETERS":
                        return SimpleResult.error(SimpleCode.SMS_INVALID_PARAM);
                    case "isv.TEMPLATE_MISSING_PARAMETERS":
                        return SimpleResult.error(SimpleCode.SMS_TEMPLATE_MISSING_PARAMETERS);
                    case "isv.BUSINESS_LIMIT_CONTROL":
                        return SimpleResult.error(SimpleCode.SMS_LIMIT_CONTROL);
                    default:
                        return new SimpleResult(SimpleCode.SMS_OTHER_ERRORS.getCode(), response.getMessage());
                }
            }
        } catch (ClientException e) {
            //客户端错误
            log.error(e, e);
            return SimpleResult.error(SimpleCode.SMS_SEND_ERROR);
        }
    }

    private String accessKey(String appId) {
        String ak = appConfigService.getString(appId, AppConfigEnum.SMS_HANDLER_ALIYUN_KEY);
        if (StringUtils.isEmpty(ak)) {
            throw new SimpleError(SimpleCode.SMS_NO_KEY);
        }
        return ak;
    }

    private String secret(String appId) {
        String sk = appConfigService.getString(appId, AppConfigEnum.SMS_HANDLER_ALIYUN_SECRET);
        if (StringUtils.isEmpty(sk)) {
            throw new SimpleError(SimpleCode.SMS_NO_SECRET);
        }
        return sk;
    }

}
