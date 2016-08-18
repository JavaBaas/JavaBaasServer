package com.javabaas.server.hook.entity;

import com.javabaas.server.user.entity.BaasUser;
import com.javabaas.server.object.entity.BaasObject;

/**
 * Created by Staryet on 15/9/24.
 */
public class HookRequest {

    private String appId;
    private BaasUser user;
    private BaasObject object;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public BaasUser getUser() {
        return user;
    }

    public void setUser(BaasUser user) {
        this.user = user;
    }

    public BaasObject getObject() {
        return object;
    }

    public void setObject(BaasObject object) {
        this.object = object;
    }
}
