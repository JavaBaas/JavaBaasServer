package com.staryet.baas.customer;

import com.staryet.baas.hook.entity.HookRequest;
import com.staryet.baas.hook.entity.HookResponse;
import com.staryet.baas.hook.entity.HookResponseCode;
import com.staryet.baas.object.entity.BaasObject;
import com.staryet.baas.object.service.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于测试云代码的模拟业务服务器环境
 * Created by Codi on 15/10/9.
 */
@RestController
@RequestMapping(value = "/customer/hook")
public class CustomerHookController {

    @Autowired
    private ObjectService objectService;

    @RequestMapping(value = "/Book/beforeInsert", method = RequestMethod.POST)
    public HookResponse beforeInsert(@RequestBody HookRequest request) {
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

    @RequestMapping(value = "/Book/afterInsert", method = RequestMethod.POST)
    public HookResponse afterInsert(@RequestBody HookRequest request) {
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

    @RequestMapping(value = "/Book/beforeUpdate", method = RequestMethod.POST)
    public HookResponse beforeUpdate(@RequestBody HookRequest request) {
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

    @RequestMapping(value = "/Book/afterUpdate", method = RequestMethod.POST)
    public HookResponse afterUpdate(@RequestBody HookRequest request) {
        HookResponse response = new HookResponse();
        response.setCode(HookResponseCode.SUCCESS);
        return response;
    }

    @RequestMapping(value = "/Book/beforeDelete", method = RequestMethod.POST)
    public HookResponse beforeDelete(@RequestBody HookRequest request) {
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

    @RequestMapping(value = "/Book/afterDelete", method = RequestMethod.POST)
    public HookResponse afterDelete(@RequestBody HookRequest request) {
        HookResponse response = new HookResponse();
        response.setCode(HookResponseCode.SUCCESS);
        return response;
    }


}
