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

    public String getAccessToken() {
        return (String) get("accessToken");
    }

    public String getUid() {
        return (String) get("uid");
    }

}
