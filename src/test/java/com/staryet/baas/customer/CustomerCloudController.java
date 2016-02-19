package com.staryet.baas.customer;

import com.staryet.baas.cloud.entity.CloudRequest;
import com.staryet.baas.cloud.entity.CloudResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于测试云代码的模拟业务服务器环境
 * Created by Codi on 15/10/9.
 */
@RestController
@RequestMapping(value = "/customer/cloud")
public class CustomerCloudController {

    @RequestMapping(value = "/function1", method = RequestMethod.POST)
    public CloudResponse function1(@RequestBody CloudRequest request) {
        CloudResponse response = new CloudResponse();
        response.setCode(0);
        response.setMessage("success");
        return response;
    }

    @RequestMapping(value = "/function2", method = RequestMethod.POST)
    public CloudResponse function2(@RequestBody CloudRequest request) {
        CloudResponse response = new CloudResponse();
        response.setCode(0);
        response.setMessage(request.getUser().getUsername());
        return response;
    }

    @RequestMapping(value = "/function3", method = RequestMethod.POST)
    public CloudResponse function3(@RequestBody CloudRequest request) {
        CloudResponse response = new CloudResponse();
        response.setCode(1);
        String param1 = request.getParams().get("param1");
        String param2 = request.getParams().get("param2");
        response.setMessage(param1 + param2);
        return response;
    }

}
