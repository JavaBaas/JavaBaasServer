package com.javabaas.server.common.entity;

import com.javabaas.server.object.entity.BaasObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Staryet on 15/6/15.
 */
public class SimpleResult extends HashMap<String, Object> {

    public static final int SUCCESS = 0;

    public static SimpleResult success() {
        return new SimpleResult(SimpleCode.SUCCESS);
    }

    public static SimpleResult error(SimpleCode simpleCode) {
        return new SimpleResult(simpleCode);
    }

    public SimpleResult(SimpleCode simpleCode) {
        this(simpleCode.getCode(), simpleCode.getMessage());
    }

    public SimpleResult(int code, String message) {
        //初始化data对象
        put("data", new BaasObject());
        setCode(code);
        setMessage(message);
    }

    public static SimpleResult fromError(SimpleError e) {
        return new SimpleResult(e.getCode(), e.getMessage());
    }

    public int getCode() {
        return (int) get("code");
    }

    public void setCode(int code) {
        put("code", code);
    }

    public String getMessage() {
        return (String) get("message");
    }

    public void setMessage(String message) {
        put("message", message);
    }

    public SimpleResult putData(String key, Object value) {
        getData().put(key, value);
        return this;
    }

    public Object getData(String key) {
        return getData().get(key);
    }

    public SimpleResult putDataAll(Map<? extends String, ?> map) {
        getData().putAll(map);
        return this;
    }

    private BaasObject getData() {
        return (BaasObject) get("data");
    }
}
