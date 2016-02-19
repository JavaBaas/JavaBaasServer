package com.staryet.baas.cloud.service;

import com.staryet.baas.admin.entity.App;
import com.staryet.baas.admin.service.AppService;
import com.staryet.baas.cloud.entity.CloudRequest;
import com.staryet.baas.cloud.entity.CloudResponse;
import com.staryet.baas.cloud.entity.CloudSetting;
import com.staryet.baas.cloud.util.SignUtil;
import com.staryet.baas.common.entity.SimpleCode;
import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.common.entity.SimpleResult;
import com.staryet.baas.user.entity.BaasUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;

/**
 * Created by Staryet on 15/9/15.
 */
@Service
public class CloudService {

    @Autowired
    private RestTemplate rest;
    @Autowired
    private AppService appService;
    @Autowired
    private SignUtil signUtil;

    public SimpleResult cloud(String appId, String plat, String functionName, BaasUser user, boolean isMaster, Map<String, String> requestParams) {
        //准备请求数据
        CloudRequest cloudRequest = new CloudRequest();
        //设置用户信息
        if (user != null) {
            user.setPassword(null);
            cloudRequest.setUser(user);
        }
        if (plat != null) {
            cloudRequest.setPlat(plat);
        }
        //设置请求参数
        cloudRequest.setAppId(appId);
        cloudRequest.setParams(requestParams);
        //将请求转发至业务服务器
        App app = appService.get(appId);
        if (app.getCloudSetting() == null || StringUtils.isEmpty(app.getCloudSetting().getCustomerHost())) {
            //未部署云代码或云代码地址为空
            throw new SimpleError(SimpleCode.CLOUD_NOT_DEPLOYED);
        } else {
            if (!app.getCloudSetting().hasFunction(functionName)) {
                //该方法未定义
                throw new SimpleError(SimpleCode.CLOUD_FUNCTION_NOT_FOUND);
            } else {
                //添加鉴权信息
                long timestamp = new Date().getTime();
                String timestampStr = String.valueOf(timestamp);
                cloudRequest.setTimestamp(timestampStr);
                if (isMaster) {
                    cloudRequest.setMasterSign(signUtil.getMasterSign(app.getId(), timestampStr));
                } else {
                    cloudRequest.setSign(signUtil.getSign(app.getId(), timestampStr));
                }
                //发送请求
                try {
                    CloudResponse response = rest.postForObject(app.getCloudSetting().getCustomerHost() + "/cloud/" + functionName, cloudRequest, CloudResponse.class);
                    SimpleResult simpleResult = SimpleResult.success();
                    simpleResult.setCode(response.getCode());
                    simpleResult.setMessage(response.getMessage());
                    if (response.getData() != null) {
                        simpleResult.putDataAll(response.getData());
                    }
                    return simpleResult;
                } catch (Exception e) {
                    throw new SimpleError(SimpleCode.CLOUD_FUNCTION_ERROR);
                }
            }
        }
    }

    public void deploy(String appId, CloudSetting setting) {
        appService.setCloudSetting(appId, setting);
    }

    public void unDeploy(String appId) {
        appService.setCloudSetting(appId, null);
    }
}
