package com.javabaas.server.sms.entity;

/**
 * 短信发送结果
 * Created by Codi on 2017/6/26.
 */
public class SmsSendResult {
    private int Code;
    private String message;

    private SmsSendResult(int code, String message) {
        Code = code;
        this.message = message;
    }

    public static SmsSendResult success() {
        return new SmsSendResult(SmsSendResultCode.SUCCESS.getCode(), SmsSendResultCode.SUCCESS.getMessage());
    }

    public static SmsSendResult error(SmsSendResultCode code) {
        return new SmsSendResult(code.getCode(), code.getMessage());
    }

    public int getCode() {
        return Code;
    }

    public void setCode(int code) {
        Code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
