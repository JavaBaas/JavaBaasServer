package com.staryet.baas.common.entity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Staryet on 15/6/15.
 */
public class SimpleResult extends HashMap<String, Object> {

    public static int SUCCESS = 0;

    public static SimpleResult success() {
        return new SimpleResult(SUCCESS, "");
    }

    public static SimpleResult error(int code) {
        return new SimpleResult(code, "");
    }

    private SimpleResult() {
    }

    public SimpleResult(SimpleCode simpleCode) {
        this(simpleCode.getCode(), simpleCode.getMessage());
    }

    public SimpleResult(int code, String message) {
        put("data", new LinkedHashMap<String, Object>());
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

    @SuppressWarnings("unchecked")
    public void putData(String key, Object value) {
        ((LinkedHashMap<String, Object>) get("data")).put(key, value);
    }

    @SuppressWarnings("unchecked")
    public Object getData(String key) {
        return ((LinkedHashMap<String, Object>) get("data")).get(key);
    }

    @SuppressWarnings("unchecked")
    public void putDataAll(Map<? extends String, ?> map) {
        ((LinkedHashMap<String, Object>) get("data")).putAll(map);
    }
}
