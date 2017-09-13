package com.javabaas.server.admin.entity;

import java.util.LinkedHashMap;

/**
 * Created by test on 2017/6/15.
 */
public class Account extends LinkedHashMap<String, Object> {

    public Account() {
        super();
    }

    public void setKey(String id) {
        put("key", id);
    }

    public String getKey() {
        return (String) get("key");
    }

    public void setSecret(String id) {
        put("secret", id);
    }

    public String getSecret() {
        return (String) get("secret");
    }
}
