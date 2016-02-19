package com.staryet.baas.user.entity;

import com.staryet.baas.object.entity.BaasObject;

import java.util.Map;

/**
 * Created by Staryet on 15/8/13.
 */
public class BaasUser extends BaasObject {

    public BaasUser() {
    }

    public BaasUser(Map<String, Object> m) {
        super(m);
    }

    public void setUsername(String username) {
        put("username", username);
    }

    public String getUsername() {
        return (String) get("username");
    }

    public void setPassword(String password) {
        put("password", password);
    }

    public String getPassword() {
        return (String) get("password");
    }

    public void setSessionToken(String sessionToken) {
        put("sessionToken", sessionToken);
    }

    public void setAuth(BaasObject auth) {
        put("auth", auth);
    }

    public BaasObject getAuth() {
        return (BaasObject) get("auth");
    }

    public String getSessionToken() {
        return (String) get("sessionToken");
    }

    public String getPhone() {
        return (String) get("phone");
    }

    public String getEmail() {
        return (String) get("email");
    }

}
