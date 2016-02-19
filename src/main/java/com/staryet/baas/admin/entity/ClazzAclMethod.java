package com.staryet.baas.admin.entity;

/**
 * Created by Staryet on 15/8/17.
 */
public enum ClazzAclMethod {

    INSERT("insert"),
    UPDATE("update"),
    FIND("find"),
    DELETE("delete");

    private String value;

    ClazzAclMethod(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
