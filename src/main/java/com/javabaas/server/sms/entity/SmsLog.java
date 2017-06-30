package com.javabaas.server.sms.entity;

import com.javabaas.server.object.entity.BaasObject;

import java.util.Map;

/**
 * Created by Codi on 2017/6/30.
 */
public class SmsLog extends BaasObject {

    public SmsLog() {
    }

    public SmsLog(Map<String, Object> m) {
        super(m);
    }

    public void setPhoneNumber(String phoneNumber) {
        put("phoneNumber", phoneNumber);
    }

    public String getPhoneNumber() {
        return getString("phoneNumber");
    }

    public void setSignName(String signName) {
        put("signName", signName);
    }

    public String getSignName() {
        return getString("signName");
    }

    public void setTemplateId(String templateId) {
        put("templateId", templateId);
    }

    public String getTemplateId() {
        return getString("templateId");
    }

    public void setParams(BaasObject params) {
        put("params", params);
    }

    public String getParams() {
        return getString("params");
    }

    public void setState(int state) {
        put("state", state);
    }

    public int getState() {
        return getInt("state");
    }


}
