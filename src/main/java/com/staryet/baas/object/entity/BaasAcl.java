package com.staryet.baas.object.entity;

import com.staryet.baas.user.entity.BaasUser;

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

    public void setReadAccess(String userId, boolean access) {
        Map<String, Boolean> accessMap = getAccessMap(userId);
        accessMap.put("read", access);
        put(userId, accessMap);
    }

    public void setWriteAccess(String userId, boolean access) {
        Map<String, Boolean> accessMap = getAccessMap(userId);
        accessMap.put("write", access);
        put(userId, accessMap);
    }

    public boolean hasWriteAccess(BaasUser user) {
        if (user == null) {
            return hasPublicWriteAccess();
        } else {
            return hasPublicWriteAccess() || hasWriteAccess(user.getId());
        }
    }

    public boolean hasWriteAccess(String name) {
        Map<String, Boolean> map = getAccessMap(name);
        Boolean write = map.get("write");
        if (write == null) {
            return false;
        } else {
            return write;
        }
    }

    public boolean hasPublicWriteAccess() {
        return hasWriteAccess("*");
    }

    private Map<String, Boolean> getAccessMap(String name) {
        Map<String, Boolean> accessMap = (Map<String, Boolean>) get(name);
        if (accessMap == null) {
            accessMap = new HashMap<>();
        }
        return accessMap;
    }

}
