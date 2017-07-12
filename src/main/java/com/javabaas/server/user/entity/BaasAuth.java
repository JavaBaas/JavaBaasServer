package com.javabaas.server.user.entity;

import com.javabaas.server.object.entity.BaasObject;

import java.util.Map;

/**
 * Created by Codi on 15/10/14.
 */
public class BaasAuth extends BaasObject {
    public BaasAuth() {

    }

    public BaasAuth(Map<String, Object> m) {
        super(m);
    }

    public void setAccessToken(String accessToken) {
        put("accessToken", accessToken);
    }

    public String getAccessToken() {
        return (String) get("accessToken");
    }

    public String getUid() {
        return (String) get("uid");
    }

    public void setUid(String uid) {
        put("uid", uid);
    }

    public String getOpenId() {
        return (String) get("openId");
    }

    public void setOpenId(String openId) {
        put("openId", openId);
    }

    public String getUnionId() {
        return (String) get("unionId");
    }

    public void setUnionId(String unionId) {
        put("unionId", unionId);
    }

    public String getEncryptedData() {
        return (String) get("encryptedData");
    }

    public String getCode() {
        return (String) get("code");
    }

    public String getIV() {
        return (String) get("iv");
    }

}
