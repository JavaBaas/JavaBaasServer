package com.javabaas.server.customer;

import com.javabaas.server.cloud.entity.CloudRequest;
import com.javabaas.server.cloud.entity.CloudResponse;
import com.javabaas.server.cloud.entity.JBRequest;
import com.javabaas.server.cloud.entity.JBResponse;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.hook.entity.HookRequest;
import com.javabaas.server.hook.entity.HookResponse;
import com.javabaas.server.hook.entity.HookResponseCode;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Codi on 2018/7/30.
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private JSONUtil jsonUtil;
    @Autowired
    private ObjectService objectService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public JBResponse api(@RequestParam String requestType, @RequestBody String body) {
        //整理请求体
        if (requestType.equals(JBRequest.REQUEST_CLOUD)) {
            //云方法
            CloudRequest cloudRequest = jsonUtil.readValue(body, CloudRequest.class);
            switch (cloudRequest.getName()) {
                case "function1":
                    return function1(cloudRequest);
                case "function2":
                    return function2(cloudRequest);
                case "function3":
                    return function3(cloudRequest);
                case "function4":
                    return function4(cloudRequest);
            }
        } else if (requestType.equals(JBRequest.REQUEST_HOOK)) {
            //钩子
            HookRequest hookRequest = jsonUtil.readValue(body, HookRequest.class);
            switch (hookRequest.getEvent()) {
                case BEFORE_INSERT:
                    return beforeInsert(hookRequest);
                case AFTER_INSERT:
                    return afterInsert(hookRequest);
                case BEFORE_UPDATE:
                    return beforeUpdate(hookRequest);
                case AFTER_UPDATE:
                    return afterUpdate(hookRequest);
                case BEFORE_DELETE:
                    return beforeDelete(hookRequest);
                case AFTER_DELETE:
                    return afterDelete(hookRequest);
            }
        }
        return null;

    }

    private CloudResponse function1(CloudRequest request) {
        CloudResponse response = new CloudResponse();
        response.setCode(0);
        response.setMessage("success");
        return response;
    }

    private CloudResponse function2(CloudRequest request) {
        CloudResponse response = new CloudResponse();
        response.setCode(0);
        response.setMessage(request.getUser().getUsername());
        return response;
    }

    private CloudResponse function3(CloudRequest request) {
        CloudResponse response = new CloudResponse();
        response.setCode(0);
        String param1 = request.getParams().get("param1");
        String param2 = request.getParams().get("param2");
        response.setMessage(param1 + param2 + request.getBody());
        return response;
    }

    private CloudResponse function4(CloudRequest request) {
        CloudResponse response = new CloudResponse();
        response.setCode(1);
        response.setMessage("error");
        return response;
    }

    private HookResponse beforeInsert(HookRequest request) {
        HookResponse response = new HookResponse();
        if (request.getObject().get("title").equals("ForeBody") || request.getObject().get("price").equals(1)) {
            //钩子中断
            response.setCode(HookResponseCode.ERROR);
        } else {
            response.setCode(HookResponseCode.SUCCESS);
        }
        if (request.getObject().get("title").equals("TwoBody")) {
            BaasObject object = request.getObject();
            object.put("title", "MineBody");
            object.put("price", 100);
            response.setObject(object);
            response.setCode(HookResponseCode.SUCCESS);
        }
        return response;
    }

    private HookResponse afterInsert(HookRequest request) {
        HookResponse response = new HookResponse();
        response.setCode(HookResponseCode.SUCCESS);
        BaasObject object = request.getObject();
        if (object.get("title").equals("StarWars")) {
            //修改数据
            object.put("title", "Obiwan");
            object.put("price", 100);
            objectService.update(request.getAppId(), "admin", "Book", object.getId(), object, null, false);
        }
        return response;
    }

    private HookResponse beforeUpdate(HookRequest request) {
        HookResponse response = new HookResponse();
        if (request.getObject().get("title").equals("ForeBody") || request.getObject().get("price").equals(1)) {
            //钩子中断
            response.setCode(HookResponseCode.ERROR);
        } else {
            response.setCode(HookResponseCode.SUCCESS);
        }
        if (request.getObject().get("title").equals("TwoBody")) {
            BaasObject object = request.getObject();
            object.put("title", "MineBody");
            object.put("price", 100);
            response.setObject(object);
            response.setCode(HookResponseCode.SUCCESS);
        }
        return response;
    }

    private HookResponse afterUpdate(HookRequest request) {
        HookResponse response = new HookResponse();
        response.setCode(HookResponseCode.SUCCESS);
        return response;
    }

    private HookResponse beforeDelete(HookRequest request) {
        HookResponse response = new HookResponse();
        if (request.getObject().get("title").equals("ThreeBody") || request.getObject().get("price").equals(1)) {
            //钩子中断
            response.setCode(HookResponseCode.ERROR);
        } else {
            response.setCode(HookResponseCode.SUCCESS);
        }
        if (request.getObject().get("title").equals("TwoBody")) {
            BaasObject object = request.getObject();
            object.put("title", "MineBody");
            object.put("price", 100);
            response.setObject(object);
            response.setCode(HookResponseCode.SUCCESS);
        }
        return response;
    }

    private HookResponse afterDelete(HookRequest request) {
        HookResponse response = new HookResponse();
        response.setCode(HookResponseCode.SUCCESS);
        return response;
    }

}
