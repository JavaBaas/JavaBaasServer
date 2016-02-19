package com.staryet.baas.push.handler.impl;

import com.staryet.baas.push.entity.Push;
import com.staryet.baas.push.handler.IPushHandler;

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
