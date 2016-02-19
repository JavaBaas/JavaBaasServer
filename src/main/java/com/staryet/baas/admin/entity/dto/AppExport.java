package com.staryet.baas.admin.entity.dto;

import com.staryet.baas.cloud.entity.CloudSetting;
import com.staryet.baas.push.entity.PushAccount;

import java.util.List;

/**
 * Created by Codi on 15/11/9.
 */
public class AppExport {

    private String id;
    private String name;
    private String key;
    private String masterKey;
    private CloudSetting cloudSetting;
    private List<ClazzExport> clazzs;
    private PushAccount pushAccount;

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

    public List<ClazzExport> getClazzs() {
        return clazzs;
    }

    public void setClazzs(List<ClazzExport> clazzs) {
        this.clazzs = clazzs;
    }

    public PushAccount getPushAccount() {
        return pushAccount;
    }

    public void setPushAccount(PushAccount pushAccount) {
        this.pushAccount = pushAccount;
    }
}
