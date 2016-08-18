package com.javabaas.server.admin.entity;

/**
 * Created by Staryet on 15/8/17.
 */
public enum ApiMethod {

    INSERT("insert"),
    UPDATE("update"),
    FIND("find"),
    DELETE("delete");

    private String value;

    ApiMethod(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ApiMethod get(String value) {
        ApiMethod[] methods = ApiMethod.class.getEnumConstants();
        for (ApiMethod method : methods) {
            if (method.value.equals(value)) {
                return method;
            }
        }
        return null;
    }

}
