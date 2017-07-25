package com.javabaas.server.cloud.handler.impl;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.cloud.entity.CloudRequest;
import com.javabaas.server.cloud.entity.CloudResponse;
import com.javabaas.server.cloud.handler.ICloudFunctionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 云代码远程执行器
 * Created by Codi on 2017/7/24.
 */
@Component
public class RemoteCloudFunctionHandler implements ICloudFunctionHandler {

    @Autowired
    private RestTemplate rest;

    @Override
    public CloudResponse cloud(App app, String name, CloudRequest request) {
        return rest.postForObject(app.getCloudSetting().getCustomerHost() + "/cloud/" + name,
                request, CloudResponse.class);
    }

}
