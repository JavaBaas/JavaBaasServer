package com.javabaas.server.cloud.entity;

import java.util.Map;

/**
 * Created by Staryet on 15/9/15.
 */
public class CloudRequest extends JBRequest {

    private Map<String, String> params;
    private String body;

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
