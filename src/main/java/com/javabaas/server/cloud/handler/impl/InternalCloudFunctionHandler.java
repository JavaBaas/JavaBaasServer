package com.javabaas.server.cloud.handler.impl;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.cloud.entity.CloudRequest;
import com.javabaas.server.cloud.entity.CloudResponse;
import com.javabaas.server.cloud.handler.ICloudFunctionHandler;

/**
 * 基于JS实现的内部云代码处理器
 * Created by Codi on 2017/7/24.
 */
public class InternalCloudFunctionHandler implements ICloudFunctionHandler {

    @Override
    public CloudResponse cloud(App app, String name, CloudRequest request) {
        return null;
    }

}
