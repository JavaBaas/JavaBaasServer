package com.staryet.baas.push.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * Created by Codi on 15/12/2.
 */
@Document
public class PushAccount {

    @NotNull(message = "key不能为空")
    private String key; //推送所使用的key
    @NotNull(message = "secret不能为空")
    private String secret; //推送使用的密钥

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
