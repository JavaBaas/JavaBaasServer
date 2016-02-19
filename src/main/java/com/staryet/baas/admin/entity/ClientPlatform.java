package com.staryet.baas.admin.entity;

/**
 * Created by Staryet on 15/8/17.
 */
public enum ClientPlatform {

    ANDROID("android"),
    IOS("ios"),
    JS("js"),
    CLOUD("cloud"),
    SHELL("shell"),
    ADMIN("admin");

    private String value;

    ClientPlatform(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ClientPlatform get(String value) {
        ClientPlatform[] platforms = ClientPlatform.class.getEnumConstants();
        for (ClientPlatform plat : platforms) {
            if (plat.value.equals(value)) {
                return plat;
            }
        }
        return null;
    }

}
