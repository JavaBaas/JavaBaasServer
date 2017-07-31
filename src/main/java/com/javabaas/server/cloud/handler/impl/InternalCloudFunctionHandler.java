package com.javabaas.server.cloud.handler.impl;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.cloud.entity.CloudRequest;
import com.javabaas.server.cloud.entity.CloudResponse;
import com.javabaas.server.cloud.handler.ICloudFunctionHandler;
import com.javabaas.server.cloud.internal.CloudEngineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * 基于JS实现的内部云代码处理器
 * Created by Codi on 2017/7/24.
 */
@Component
public class InternalCloudFunctionHandler implements ICloudFunctionHandler {

    @Autowired
    private CloudEngineManager engineManager;

    @Override
    public CloudResponse cloud(App app, String name, CloudRequest request) {
        ScriptEngine engine = null;
        try {
            engine = engineManager.getCloudEngine(app.getId(), name);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        Invocable invocable = (Invocable) engine;
        CloudResponse response = new CloudResponse();
        try {
            invocable.invokeFunction(name, request, response);
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return response;
    }

}
