package com.staryet.baas.cloud.controller;

import com.staryet.baas.cloud.entity.CloudSetting;
import com.staryet.baas.cloud.service.CloudService;
import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.common.entity.SimpleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Staryet on 15/9/22.
 */
@RestController
@RequestMapping(value = "/api/master/cloud")
public class CloudMasterController {

    @Autowired
    private CloudService cloudService;

    /**
     * 部署云代码相关配置
     *
     * @param appId   应用id
     * @param setting 云代码 配置信息
     * @return 结果
     * @throws SimpleError
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult deploy(@RequestHeader(value = "JB-AppId") String appId, @RequestBody CloudSetting setting) {
        cloudService.deploy(appId, setting);
        return SimpleResult.success();
    }

    /**
     * 删除云代码配置
     *
     * @param appId 应用id
     * @return 结果
     * @throws SimpleError
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @ResponseBody
    public SimpleResult delete(@RequestHeader(value = "JB-AppId") String appId) {
        cloudService.unDeploy(appId);
        return SimpleResult.success();
    }

}
