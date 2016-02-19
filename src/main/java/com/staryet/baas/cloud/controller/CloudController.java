package com.staryet.baas.cloud.controller;

import com.staryet.baas.cloud.service.CloudService;
import com.staryet.baas.common.entity.SimpleResult;
import com.staryet.baas.common.service.MasterService;
import com.staryet.baas.user.entity.BaasUser;
import com.staryet.baas.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 云方法
 */
@RestController
@RequestMapping(value = "/api/cloud")
public class CloudController {

    @Autowired
    private CloudService cloudService;
    @Autowired
    private MasterService masterService;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/{name}")
    @ResponseBody
    public SimpleResult cloud(@RequestHeader(value = "JB-AppId") String appId,
                              @RequestHeader(value = "JB-Plat") String plat,
                              @PathVariable String name) {
        //获取登录用户
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        //整理请求参数
        Map<String, String> requestParams = new HashMap<>();
        Set<String> keys = request.getParameterMap().keySet();
        for (String key : keys) {
            requestParams.put(key, request.getParameter(key));
        }
        return cloudService.cloud(appId, plat, name, currentUser, isMaster, requestParams);
    }

}
