package com.javabaas.server.sms.handler.impl.aliyun;

import com.aliyuncs.RpcAcsRequest;

public class SendSmsRequest
        extends RpcAcsRequest<SendSmsResponse> {
    private String outId;
    private String signName;
    private Long ownerId;
    private Long resourceOwnerId;
    private String templateCode;
    private String phoneNumbers;
    private String resourceOwnerAccount;
    private String templateParam;

    public SendSmsRequest() {
        super("Dysmsapi", "2017-05-25", "SendSms");
    }

    public String getOutId() {
        return this.outId;
    }

    public void setOutId(String outId) {
        this.outId = outId;
        putQueryParameter("OutId", outId);
    }

    public String getSignName() {
        return this.signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
        putQueryParameter("SignName", signName);
    }

    public Long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
        putQueryParameter("OwnerId", ownerId);
    }

    public Long getResourceOwnerId() {
        return this.resourceOwnerId;
    }

    public void setResourceOwnerId(Long resourceOwnerId) {
        this.resourceOwnerId = resourceOwnerId;
        putQueryParameter("ResourceOwnerId", resourceOwnerId);
    }

    public String getTemplateCode() {
        return this.templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
        putQueryParameter("TemplateCode", templateCode);
    }

    public String getPhoneNumbers() {
        return this.phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
        putQueryParameter("PhoneNumbers", phoneNumbers);
    }

    public String getResourceOwnerAccount() {
        return this.resourceOwnerAccount;
    }

    public void setResourceOwnerAccount(String resourceOwnerAccount) {
        this.resourceOwnerAccount = resourceOwnerAccount;
        putQueryParameter("ResourceOwnerAccount", resourceOwnerAccount);
    }

    public String getTemplateParam() {
        return this.templateParam;
    }

    public void setTemplateParam(String templateParam) {
        this.templateParam = templateParam;
        putQueryParameter("TemplateParam", templateParam);
    }

    public Class<SendSmsResponse> getResponseClass() {
        return SendSmsResponse.class;
    }
}

