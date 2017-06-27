package com.javabaas.server.user.entity;

/**
 * Created by test on 2017/6/12.
 */
public enum BaasSnsType {
    WEIBO(1, "weibo"),
    QQ(2, "qq"),
    WEIXIN(3, "wx"),
    WEBAPP(4, "wx");

    private int code;
    private String value;

    BaasSnsType(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

    public static BaasSnsType getType(int code) {
        for (BaasSnsType baasSnsType :  BaasSnsType.values()) {
            if (baasSnsType.code == code) {
                return baasSnsType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + " code:" + this.code + " value:" + this.value;
    }
}
