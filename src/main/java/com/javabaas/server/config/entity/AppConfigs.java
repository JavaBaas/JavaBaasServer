package com.javabaas.server.config.entity;

import com.javabaas.server.admin.entity.App;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用配置
 * Created by Codi on 2017/7/8.
 */
@Document
public class AppConfigs {

    private String id;
    @DBRef
    private App app;
    private Map<String, String> params;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setParam(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
    }

    public String getParam(String key) {
        if (params == null) {
            return null;
        } else {
            return params.get(key);
        }
    }

}
