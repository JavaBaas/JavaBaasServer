package com.javabaas.server.cloud.handler;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.cloud.entity.CloudRequest;
import com.javabaas.server.cloud.entity.CloudResponse;

/**
 * Created by Codi on 2017/7/24.
 */
public interface ICloudFunctionHandler {

    CloudResponse cloud(App app, String name, CloudRequest request);

}
