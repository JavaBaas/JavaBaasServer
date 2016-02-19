package com.staryet.baas.cloud.entity;

import com.staryet.baas.user.entity.BaasUser;

import java.util.Map;

/**
 * Created by Staryet on 15/9/15.
 */
public class CloudRequest {

    private String appId;
    private String plat;
    private BaasUser user;
    private String sign;
    private String timestamp;
    private String masterSign;
    private Map<String, String> params;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPlat() {
        return plat;
    }

    public void setPlat(String plat) {
        this.plat = plat;
    }

    public BaasUser getUser() {
        return user;
    }

    public void setUser(BaasUser user) {
        this.user = user;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMasterSign() {
        return masterSign;
    }

    public void setMasterSign(String masterSign) {
        this.masterSign = masterSign;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
