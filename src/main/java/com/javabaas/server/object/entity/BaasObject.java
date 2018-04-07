package com.javabaas.server.object.entity;

import com.javabaas.server.user.entity.BaasUser;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Staryet on 15/6/23.
 */
public class BaasObject extends LinkedHashMap<String, Object> {

    public BaasObject() {
        super();
    }

    public BaasObject(Map<String, Object> m) {
        super(m);
    }

    public BaasObject(String key, Object value) {
        this.put(key, value);
    }

    public void setId(String id) {
        put("_id", id);
    }

    public String getId() {
        return (String) get("_id");
    }

    public BaasAcl getAcl() {
        return (BaasAcl) get("acl");
    }

    public void setAcl(BaasAcl acl) {
        put("acl", acl);
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public boolean getBoolean(String key) {
        Boolean b = (Boolean) get(key);
        return b == null ? false : b;
    }

    public long getLong(String key) {
        Object value = get(key);
        return value == null ? 0 : Long.valueOf(value.toString());
    }

    public int getInt(String key) {
        Object value = get(key);
        return value == null ? 0 : Integer.valueOf(value.toString());
    }

    public BaasList getList(String key) {
        Object value = get(key);
        return value == null ? null : (BaasList) value;
    }

    @SuppressWarnings("unchecked")
    public BaasObject getBaasObject(String key) {
        Object value = get(key);
        Map<String, Object> object = null;
        try {
            object = (Map<String, Object>) value;
        } catch (ClassCastException ignored) {
        }
        return object == null ? null : new BaasObject(object);
    }

    @SuppressWarnings("unchecked")
    public BaasUser getBaasUser(String key) {
        Object value = get(key);
        Map<String, Object> object = null;
        try {
            object = (Map<String, Object>) value;
        } catch (ClassCastException ignored) {
        }
        return object == null ? null : new BaasUser(object);
    }

    public Date getCreatedAt() {
        Date date = new Date();
        date.setTime(getLong("createdAt"));
        return date;
    }

    public Date getUpdatedAt() {
        Date date = new Date();
        date.setTime(getLong("updatedAt"));
        return date;
    }

    public Object getCreatedPlatform() {
        return get("createdPlat");
    }

    public Object getUpdatedPlatform() {
        return get("updatedPlat");
    }

}
