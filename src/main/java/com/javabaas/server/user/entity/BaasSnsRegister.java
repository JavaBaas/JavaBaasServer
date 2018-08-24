package com.javabaas.server.user.entity;

import com.javabaas.server.object.entity.BaasObject;

import javax.validation.constraints.NotEmpty;

/**
 * 社交平台注册登录
 * Created by Codi on 2017/7/11.
 */
public class BaasSnsRegister extends BaasObject {

    @NotEmpty(message = "授权信息不能为空")
    public BaasAuth getAuth() {
        BaasObject auth = getBaasObject("auth");
        return auth == null ? null : new BaasAuth(auth);
    }

    public BaasUser getUser() {
        return getBaasUser("user");
    }

}
