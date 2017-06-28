package com.javabaas.server.push.handler.impl;

import com.javabaas.server.push.entity.Push;
import com.javabaas.server.push.entity.PushMessage;
import com.javabaas.server.push.entity.PushNotification;
import com.javabaas.server.push.handler.IPushHandler;

import java.util.Collection;

/**
 * Created by Codi on 16/1/11.
 */
public class TestPushHandler implements IPushHandler {

    @Override
    public void pushSingle(String appId, String id, Push push) {

    }

    @Override
    public void pushMulti(String appId, Collection<String> ids, Push push) {

    }

    @Override
    public void pushAll(String appId, Push push) {

    }
}
