package com.javabaas.server.user.util;

/**
 * Created by Codi on 2017/7/4.
 */
public class UUID {

    public static String uuid() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

}
