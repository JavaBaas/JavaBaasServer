package com.javabaas.server.object.util;

import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Created by Codi on 2017/9/12.
 */
public class BaasObjectIdUtil {

    public static String createId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static boolean isValidId(String id) {
        return !StringUtils.isEmpty(id) && id.length() == 32;
    }
}
