package com.staryet.baas.user.service;

import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.object.service.ObjectService;
import com.staryet.baas.user.entity.BaasInstallation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Staryet on 15/8/13.
 */
@Service
public class InstallationService {

    public static String INSTALLATION_CLASS_NAME = "_Installation";
    @Autowired
    private ObjectService objectService;

    /**
     * 注册设备
     *
     * @param installation 设备信息
     * @return id
     * @throws SimpleError
     */
    public String register(String appId, String plat, BaasInstallation installation) {
        //禁止设置ACL字段
        installation.remove("acl");
        return objectService.insert(appId, plat, INSTALLATION_CLASS_NAME, installation, null, true).getId();
    }

}
