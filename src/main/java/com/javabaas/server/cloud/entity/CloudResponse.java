package com.javabaas.server.cloud.entity;

import java.util.Map;

/**
 * Created by Staryet on 15/9/15.
 */
public class CloudResponse extends JBResponse {

    public Map<String, Object> data;

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
