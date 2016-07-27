package com.javabaas.server.hook.entity;

import com.javabaas.server.object.entity.BaasObject;

/**
 * Created by Staryet on 15/9/24.
 */
public class HookResponse {

    public int code;
    public String result;
    public BaasObject object;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public BaasObject getObject() {
        return object;
    }

    public void setObject(BaasObject object) {
        this.object = object;
    }
}
