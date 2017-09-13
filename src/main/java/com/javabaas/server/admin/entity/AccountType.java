package com.javabaas.server.admin.entity;

/**
 * Created by test on 2017/6/19.
 */
public enum AccountType {
    PUSH(1, "push"),
    WEBAPP(2, "webapp");

    private int code;
    private String value;

    AccountType(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

    public static AccountType getType(int code) {
        for (AccountType accountType : AccountType.values()) {
            if (accountType.code == code) {
                return accountType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + " code:" + this.code + " value:" + this.value;
    }
}
