package com.javabaas.server.config.controller;

import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.config.entity.AppConfig;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 应用配置接口
 * Created by Codi on 2017/7/6.
 */
@RestController
@RequestMapping(value = "/api/master/config")
public class AppConfigController {

    @Autowired
    private AppConfigService appConfigService;

    /**
     * 设置应用配置
     *
     * @param appId  应用id
     * @param config 配置
     */
    @RequestMapping(value = "/app", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult setAppConfig(@RequestHeader(value = "JB-AppId") String appId,
                                     @Valid @RequestBody AppConfig config) {
        appConfigService.setConfig(appId, config.getKey(), config.getValue());
        return SimpleResult.success();
    }

    /**
     * 获取应用配置
     *
     * @param appId 应用id
     */
    @RequestMapping(value = "/app/{key}", method = RequestMethod.GET)
    @ResponseBody
    public String getAppConfig(@RequestHeader(value = "JB-AppId") String appId,
                               @PathVariable String key) {
        return appConfigService.getString(appId, key);
    }

    /**
     * 获取所有可用的应用配置
     *
     * @return 配置
     */
    @RequestMapping(value = "app/configs", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> getAppConfigs() {
        List<Map<String, String>> result = new LinkedList<>();
        for (AppConfigEnum appConfigEnum : AppConfigEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("key", appConfigEnum.getKey());
            map.put("name", appConfigEnum.getName());
            result.add(map);
        }
        return result;
    }


}
