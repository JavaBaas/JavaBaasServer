package com.staryet.baas.admin.entity;

import com.staryet.baas.user.entity.BaasUser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Staryet on 15/8/17.
 */
public class ClazzAcl extends LinkedHashMap<String, Object> {

    public void setPublicAccess(ClazzAclMethod method, boolean access) {
        Map<String, Boolean> accessMap = getAccessMap("*");
        accessMap.put(method.toString(), access);
        put("*", accessMap);
    }

    public void setAccess(ClazzAclMethod method, String userId, boolean access) {
        Map<String, Boolean> accessMap = getAccessMap(userId);
        accessMap.put(method.toString(), access);
        put(userId, accessMap);
    }

    public boolean hasAccess(ClazzAclMethod method, BaasUser user) {
        if (user == null) {
            return hasPublicAccess(method);
        } else {
            return hasPublicAccess(method) || hasAccess(method, user.getId());
        }
    }

    public boolean hasPublicAccess(ClazzAclMethod method) {
        return hasAccess(method, "*");
    }

    public boolean hasAccess(ClazzAclMethod method, String name) {
        Map<String, Boolean> map = getAccessMap(name);
        Boolean write = map.get(method.toString());
        if (write == null) {
            return false;
        } else {
            return write;
        }
    }

    private Map<String, Boolean> getAccessMap(String name) {
        Map<String, Boolean> accessMap = (Map<String, Boolean>) get(name);
        if (accessMap == null) {
            accessMap = new HashMap<>();
        }
        return accessMap;
    }

}
