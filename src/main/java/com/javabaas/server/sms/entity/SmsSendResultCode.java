package com.javabaas.server.sms.entity;

/**
 * 短信发送结果返回码
 * Created by Codi on 2017/6/26.
 */
public enum SmsSendResultCode {

    OK(0, "成功"),
    ILLEGAL_PHONE_NUMBER(1, "手机号不合法"),
    AMOUNT_NOT_ENOUGH(2, "余额不足"),
    INVALID_PARAM(3, "参数不合法");

    private int code;
    private String message;

    SmsSendResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
