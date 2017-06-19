package com.javabaas.server.admin.entity;

import com.javabaas.server.cloud.entity.CloudSetting;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Staryet on 15/9/17.
 */
@Document
public class App {

    private String id;
    private String name;
    private String key;
    private String masterKey;
    private CloudSetting cloudSetting;
    private AppAccounts appAccounts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }

    public CloudSetting getCloudSetting() {
        return cloudSetting;
    }

    public void setCloudSetting(CloudSetting cloudSetting) {
        this.cloudSetting = cloudSetting;
    }

    public AppAccounts getAppAccounts() {
        return appAccounts;
    }

    public void setAppAccounts(AppAccounts appAccounts) {
        this.appAccounts = appAccounts;
    }
}
