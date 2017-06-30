package com.javabaas.server.common.entity;

/**
 * Created by Staryet on 15/6/18.
 */
public class SimpleError extends RuntimeException {

    private int code;
    private String message;

    public static void e(SimpleCode simpleCode) {
        throw new SimpleError(simpleCode);
    }

    public SimpleError(SimpleCode simpleCode) {
        code = simpleCode.getCode();
        message = simpleCode.getMessage();
    }

    public SimpleError(int code) {
        this.code = code;
        this.message = "";
    }

    public SimpleError(int code, String message) {
        this.code = code;
        this.message = message;
    }

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
