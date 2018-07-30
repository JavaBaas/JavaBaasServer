package com.javabaas.server.hook.entity;

import com.javabaas.server.cloud.entity.JBRequest;
import com.javabaas.server.object.entity.BaasObject;

/**
 * Created by Staryet on 15/9/24.
 */
public class HookRequest extends JBRequest {

    private HookEvent event;
    private BaasObject object;

    public HookEvent getEvent() {
        return event;
    }

    public void setEvent(HookEvent event) {
        this.event = event;
    }

    public BaasObject getObject() {
        return object;
    }

    public void setObject(BaasObject object) {
        this.object = object;
    }
}
