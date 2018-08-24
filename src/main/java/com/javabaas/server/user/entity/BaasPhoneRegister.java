package com.javabaas.server.user.entity;

import com.javabaas.server.object.entity.BaasObject;

import javax.validation.constraints.NotEmpty;

/**
 * 手机号注册对象
 * Created by Codi on 2017/6/26.
 */
public class BaasPhoneRegister extends BaasObject {

    public BaasPhoneRegister() {
    }

    @NotEmpty(message = "手机号不能为空")
    public String getPhone() {
        return getString("phone");
    }

    public void setPhone(String phone) {
        put("phone", phone);
    }

    @NotEmpty(message = "验证码不能为空")
    public String getCode() {
        return getString("code");
    }

    public void setCode(String code) {
        put("code", code);
    }
}
