package com.javabaas.server.config.entity;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * 应用配置
 * Created by Codi on 2017/7/21.
 */
public class AppConfig {
    @NotEmpty
    private String key;
    @NotEmpty
    private String value;

    public AppConfig() {
    }

    public AppConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getNoDotKey() {
        return key.replaceAll("\\.", "");
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
