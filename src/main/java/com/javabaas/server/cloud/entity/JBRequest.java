package com.javabaas.server.cloud.entity;

import com.javabaas.server.user.entity.BaasUser;

/**
 * Created by Codi on 2018/7/30.
 */
public class JBRequest {
    public static final String REQUEST_CLOUD = "1";
    public static final String REQUEST_HOOK = "2";

    private String name;
    private String appId;
    private String plat;
    private BaasUser user;
    private String timestamp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
