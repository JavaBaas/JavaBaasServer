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
        appConfigService.setConfig(appId, config);
        return SimpleResult.success();
    }

    /**
     * 获取应用配置
     *
     * @param appId 应用id
     */
    @RequestMapping(value = "/app", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult getAppConfig(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestParam String key) {
        String value = appConfigService.getString(appId, key);
        Map<String, Object> config = new HashMap<>();
        config.put(key, value);
        SimpleResult result = SimpleResult.success();
        result.putData("result", config);
        return result;
    }

    /**
     * 获取所有可用的应用配置
     *
     * @return 配置
     */
    @RequestMapping(value = "app/configs", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult getAppConfigs() {
        List<Map<String, String>> list = new LinkedList<>();
        for (AppConfigEnum appConfigEnum : AppConfigEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("key", appConfigEnum.getKey());
            map.put("name", appConfigEnum.getName());
            list.add(map);
        }
        SimpleResult result = SimpleResult.success();
        result.putData("result", list);
        return result;
    }


}
