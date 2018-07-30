package com.javabaas.server.cloud.entity;

/**
 * Created by Codi on 2018/7/30.
 */
public class JBResponse {
    private int code;
    private String message;

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
}
