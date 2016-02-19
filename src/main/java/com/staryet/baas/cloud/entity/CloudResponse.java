package com.staryet.baas.cloud.entity;

import java.util.Map;

/**
 * Created by Staryet on 15/9/15.
 */
public class CloudResponse {

    public int code;
    public String message;
    public Map<String, Object> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
