package com.staryet.baas.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.common.entity.SimpleResult;
import com.staryet.baas.user.entity.BaasInstallation;
import com.staryet.baas.user.service.InstallationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 注册设备
 * Created by Staryet on 15/8/13.
 */
@RestController
@RequestMapping(value = "/api/installation")
public class InstallationController {

    @Autowired
    private InstallationService installationService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 注册设备
     *
     * @param body
     * @return id
     * @throws SimpleError
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult insert(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @RequestBody String body) throws IOException {
        BaasInstallation installation = objectMapper.readValue(body, BaasInstallation.class);
        String id = installationService.register(appId, plat, installation);
        SimpleResult result = SimpleResult.success();
        result.putData("id", id);
        result.putData("_id", id);
        return result;
    }

}
