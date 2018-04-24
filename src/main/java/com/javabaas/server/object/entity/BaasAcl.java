package com.javabaas.server.object.entity;

import com.javabaas.server.role.entity.BaasRole;
import com.javabaas.server.user.entity.BaasUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Staryet on 15/8/13.
 */
public class BaasAcl extends BaasObject {

    public BaasAcl() {
    }

    public BaasAcl(Map<String, Object> m) {
        super(m);
    }

    public BaasAcl(String key, Object value) {
        super(key, value);
    }

    public void setPublicReadAccess(boolean access) {
        Map<String, Boolean> accessMap = getAccessMap("*");
        accessMap.put("read", access);
        put("*", accessMap);
    }

    public void setPublicWriteAccess(boolean access) {
        Map<String, Boolean> accessMap = getAccessMap("*");
        accessMap.put("write", access);
        put("*", accessMap);
    }

    public void setReadAccess(BaasUser user, boolean access) {
        Map<String, Boolean> accessMap = getAccessMap(user.getId());
        accessMap.put("read", access);
        put(user.getId(), accessMap);
    }

    public void setReadAccess(BaasRole role, boolean access) {
        Map<String, Boolean> accessMap = getAccessMap("role:" + role.getName());
        accessMap.put("read", access);
        put("role:" + role.getName(), accessMap);
    }

    public void setWriteAccess(BaasUser user, boolean access) {
        Map<String, Boolean> accessMap = getAccessMap(user.getId());
        accessMap.put("write", access);
        put(user.getId(), accessMap);
    }

    public void setWriteAccess(BaasRole role, boolean access) {
        Map<String, Boolean> accessMap = getAccessMap("role:" + role.getName());
        accessMap.put("write", access);
        put("role:" + role.getName(), accessMap);
    }

    public Map<String, Boolean> getAccessMap(String name) {
        Map<String, Boolean> accessMap = (Map<String, Boolean>) get(name);
        if (accessMap == null) {
            accessMap = new HashMap<>();
        }
        return accessMap;
    }

}
