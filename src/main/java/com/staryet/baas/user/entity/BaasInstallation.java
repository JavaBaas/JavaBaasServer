package com.staryet.baas.user.entity;

import com.staryet.baas.object.entity.BaasObject;

import java.util.Map;

/**
 * Created by Staryet on 15/8/13.
 */
public class BaasInstallation extends BaasObject {

    public BaasInstallation() {
    }

    public BaasInstallation(Map<String, Object> m) {
        super(m);
    }

    public void setInstallationId(String installationId) {
        put("installationId", installationId);
    }

    public String getInstallationId() {
        return getString("installationId");
    }

    public void setDeviceType(String deviceType) {
        put("deviceType", deviceType);
    }

    public String getDeviceType() {
        return getString("deviceType");
    }

    public void setDeviceToken(String deviceToken) {
        put("deviceToken", deviceToken);
    }

    public String getDeviceToken() {
        return getString("deviceToken");
    }

}
