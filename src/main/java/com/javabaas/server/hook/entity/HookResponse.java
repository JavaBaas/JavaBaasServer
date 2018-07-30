package com.javabaas.server.hook.entity;

import com.javabaas.server.cloud.entity.JBResponse;
import com.javabaas.server.object.entity.BaasObject;

/**
 * Created by Staryet on 15/9/24.
 */
public class HookResponse extends JBResponse {

    public BaasObject object;

    public BaasObject getObject() {
        return object;
    }

    public void setObject(BaasObject object) {
        this.object = object;
    }
}
