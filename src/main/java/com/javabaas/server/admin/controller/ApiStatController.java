package com.javabaas.server.admin.controller;

import com.javabaas.server.admin.entity.ApiMethod;
import com.javabaas.server.admin.entity.ClientPlatform;
import com.javabaas.server.admin.service.StatService;
import com.javabaas.server.common.entity.SimpleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API统计
 * Created by Codi on 15/10/20.
 */
@RestController
@RequestMapping(value = "/api/master/apiStat")
public class ApiStatController {

    @Autowired
    private StatService statService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult getStat(@RequestHeader(value = "JB-AppId") String appId,
                                @RequestParam(required = false) String method,
                                @RequestParam(required = false) String clazz,
                                @RequestParam(required = false) String plat,
                                @RequestParam String from,
                                @RequestParam String to) {
        ApiMethod apiMethod = ApiMethod.get(method);
        ClientPlatform clientPlatform = ClientPlatform.get(plat);
        List<Long> list = statService.get(appId, apiMethod, clazz, clientPlatform, from, to);
        SimpleResult result = SimpleResult.success();
        result.putData("result", list);
        return result;
    }

}
