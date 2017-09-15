package com.javabaas.server.admin.entity;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Codi on 2017/9/13.
 */
public class InternalFields {

    public static List<String> fields() {
        return Arrays.asList("_id", "createdAt", "updatedAt", "createdPlat", "updatedPlat", "acl");
    }

}
