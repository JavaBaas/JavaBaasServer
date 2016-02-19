package com.staryet.baas.admin.controller;

import com.staryet.baas.admin.entity.ApiMethod;
import com.staryet.baas.admin.entity.ClientPlatform;
import com.staryet.baas.admin.service.StatService;
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
    public List<Long> getStat(@RequestHeader(value = "JB-AppId") String appId,
                              @RequestParam(required = false) String method,
                              @RequestParam(required = false) String clazz,
                              @RequestParam(required = false) String plat,
                              @RequestParam String from,
                              @RequestParam String to) {
        ApiMethod apiMethod = ApiMethod.get(method);
        ClientPlatform clientPlatform = ClientPlatform.get(plat);
        return statService.get(appId, apiMethod, clazz, clientPlatform, from, to);
    }

}
