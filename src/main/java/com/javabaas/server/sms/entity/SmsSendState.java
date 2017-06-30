package com.javabaas.server.sms.entity;

/**
 * 短信发送状态
 * Created by Codi on 2017/6/30.
 */
public enum SmsSendState {

    WAIT(0),//等待发送
    SUCCESS(1), //发送成功
    FAIL(2);//失败

    private int code;

    SmsSendState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
