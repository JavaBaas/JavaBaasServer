package com.javabaas.server.cloud.internal;

import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Codi on 2017/7/25.
 */
@Service
public class JB {

    @Autowired
    private ObjectService objectService;

    public BaasObject get(String className, String id) {
        return objectService.get("597729ca4fdda62044e7cb2c", "cloud", className, id);
    }

    public void log(Object object) {
        System.out.println(object);
    }

}
