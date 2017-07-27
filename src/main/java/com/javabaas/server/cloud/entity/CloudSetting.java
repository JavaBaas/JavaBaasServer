package com.javabaas.server.cloud.entity;

import java.util.List;
import java.util.Map;

/**
 * 云代码配置
 * Created by Staryet on 15/9/22.
 */
public class CloudSetting {

    private String customerHost;
    private List<String> cloudFunctions;
    private Map<String, HookSetting> hookSettings;

    public String getCustomerHost() {
        return customerHost;
    }

    public void setCustomerHost(String customerHost) {
        this.customerHost = customerHost;
    }

    public List<String> getCloudFunctions() {
        return cloudFunctions;
    }

    public void setCloudFunctions(List<String> cloudFunctions) {
        this.cloudFunctions = cloudFunctions;
    }

    public Map<String, HookSetting> getHookSettings() {
        return hookSettings;
    }

    public void setHookSettings(Map<String, HookSetting> hookSettings) {
        this.hookSettings = hookSettings;
    }

    public HookSetting getHookSetting(String name) {
        return hookSettings == null ? null : hookSettings.get(name);
    }

    public boolean hasFunction(String name) {
        if (cloudFunctions == null) {
            return false;
        } else {
            for (String function : cloudFunctions) {
                if (function.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

}
