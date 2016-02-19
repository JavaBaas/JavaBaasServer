package com.staryet.baas.cloud.util;

import com.staryet.baas.admin.entity.App;
import com.staryet.baas.admin.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * Created by Codi on 16/2/17.
 */
@Component
public class SignUtil {

    @Autowired
    private AppService appService;

    public String getSign(String appId, String timestamp) {
        App app = appService.get(appId);
        return DigestUtils.md5DigestAsHex((app.getKey() + ":" + timestamp).getBytes());
    }

    public String getMasterSign(String appId, String timestamp) {
        App app = appService.get(appId);
        return DigestUtils.md5DigestAsHex((app.getMasterKey() + ":" + timestamp).getBytes());
    }

}
