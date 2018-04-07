package com.javabaas.server.common.controller;

import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.common.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 获取服务器时间 状态
 * Created by Codi on 15/10/21.
 */
@RestController
@RequestMapping(value = "/")
public class StatusController {

    @Autowired
    private HealthEndpoint healthEndpoint;
    @Autowired
    private TimeService timeService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public Map<String, Object> time() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("time", new Date().getTime());
        result.put("started", timeService.getStartedTime());
        result.put("health", healthEndpoint.health());
        return result;
    }

    @RequestMapping(value = "api", method = RequestMethod.GET)
    public SimpleResult serverTime() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("time", new Date().getTime());
        data.put("started", timeService.getStartedTime());
        data.put("health", healthEndpoint.health());
        SimpleResult result = SimpleResult.success();
        result.putData("result", data);
        return result;
    }

}
