package com.javabaas.server.user.entity;

/**
 * Created by Codi on 2017/6/26.
 */
public class BaasPhoneRegister {

    private String phoneNumber;
    private String smsCode;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }
}
