package com.javabaas.server.role.entity;

import com.javabaas.server.object.entity.BaasObject;

import java.util.List;
import java.util.Map;

/**
 * Created by zangyilin on 2018/4/16.
 */
public class BaasRole extends BaasObject {
    public BaasRole() {
        super();
    }

    public BaasRole(Map<String, Object> m) {
        super(m);
    }

    public void setName(String name) {
        put("name", name);
    }

    public String getName() {
        return (String) get("name");
    }

    public void setRoles(List<String> roles) {
        put("roles", roles);
    }

    public List<String> getRoles() {
        return get("roles") == null ? null : (List<String>) get("roles");
    }

    public void setUsers(List<String> users) {
        put("users", users);
    }

    public List<String> getUsers() {
        return get("users") == null ? null : (List<String>) get("users");
    }
}
