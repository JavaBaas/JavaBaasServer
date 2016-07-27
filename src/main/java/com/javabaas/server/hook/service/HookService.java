package com.javabaas.server.hook.service;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.cloud.entity.CloudSetting;
import com.javabaas.server.cloud.entity.HookSetting;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.hook.entity.HookEvent;
import com.javabaas.server.hook.entity.HookRequest;
import com.javabaas.server.hook.entity.HookResponse;
import com.javabaas.server.hook.entity.HookResponseCode;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.user.entity.BaasUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 钩子服务
 */
@Service
public class HookService {

    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private RestTemplate rest;
    @Autowired
    private AppService appService;

    public void hook(HookEvent event, String appId, String className, BaasObject object, BaasUser user) {
        if (event == HookEvent.BEFORE_INSERT || event == HookEvent.BEFORE_UPDATE || event == HookEvent.BEFORE_DELETE) {
            beforeHook(event, appId, className, object, user);
        } else if (event == HookEvent.AFTER_INSERT || event == HookEvent.AFTER_UPDATE || event == HookEvent.AFTER_DELETE) {
            afterHook(event, appId, className, object, user);
        }
    }

    public void beforeInsert(String appId, String className, BaasObject object, BaasUser user) {
        hook(HookEvent.BEFORE_INSERT, appId, className, object, user);
    }

    public void afterInsert(String appId, String className, BaasObject object, BaasUser user) {
        hook(HookEvent.AFTER_INSERT, appId, className, object, user);
    }

    public void beforeUpdate(String appId, String className, BaasObject object, BaasUser user) {
        hook(HookEvent.BEFORE_UPDATE, appId, className, object, user);
    }

    public void afterUpdate(String appId, String className, BaasObject object, BaasUser user) {
        hook(HookEvent.AFTER_UPDATE, appId, className, object, user);
    }

    public void beforeDelete(String appId, String className, BaasObject object, BaasUser user) {
        hook(HookEvent.BEFORE_DELETE, appId, className, object, user);
    }

    public void afterDelete(String appId, String className, BaasObject object, BaasUser user) {
        hook(HookEvent.AFTER_DELETE, appId, className, object, user);
    }

    private void beforeHook(HookEvent event, String appId, String className, BaasObject object, BaasUser user) {
        App app = appService.get(appId);
        CloudSetting setting = app.getCloudSetting();
        if (hasHook(setting, className, event)) {
            //已配置云方法
            log.debug("App:" + appId + " Hook:" + event.getName() + " class:" + className + " _id:" + object.getId());
            HookRequest request = new HookRequest();
            request.setAppId(appId);
            request.setUser(user);
            request.setObject(object);
            try {
                HookResponse response = rest.postForObject(app.getCloudSetting().getCustomerHost() + "/hook/" + className + "/" + event.getName(), request, HookResponse.class);
                if (response != null) {
                    //执行成功
                    if (response.getCode() == HookResponseCode.ERROR) {
                        //钩子中断
                        throw new SimpleError(SimpleCode.HOOK_INTERCEPTION);
                    } else {
                        //成功
                        if (response.getObject() != null) {
                            object.putAll(response.getObject());
                        }
                    }
                }
            } catch (RestClientException e) {
                //执行失败直接返回
                log.debug("App:" + appId + " HookFail:" + event.getName() + " class:" + className + " _id:" + object.getId());
            }
        }
    }

    private void afterHook(HookEvent event, String appId, String className, BaasObject object, BaasUser user) {
        App app = appService.get(appId);
        CloudSetting setting = app.getCloudSetting();
        if (hasHook(setting, className, event)) {
            //已配置云方法
            log.debug("App:" + appId + " Hook:" + event.getName() + " class:" + className + " _id:" + object.getId());
            HookRequest request = new HookRequest();
            request.setAppId(appId);
            request.setUser(user);
            request.setObject(object);
            try {
                rest.postForObject(app.getCloudSetting().getCustomerHost() + "/hook/" + className + "/" + event.getName(), request, HookResponse.class);
            } catch (RestClientException e) {
                //执行失败直接返回
                log.debug("App:" + appId + " HookFail:" + event.getName() + " class:" + className + " _id:" + object.getId());
            }
        }
    }

    private boolean hasHook(CloudSetting setting, String className, HookEvent event) {
        if (setting == null ||
                StringUtils.isEmpty(setting.getCustomerHost()) ||
                setting.getHookSetting(className) == null) {
            return false;
        } else {
            HookSetting hookSetting = setting.getHookSetting(className);
            switch (event) {
                case BEFORE_INSERT:
                    return hookSetting.isBeforeInsert();
                case AFTER_INSERT:
                    return hookSetting.isAfterInsert();
                case BEFORE_UPDATE:
                    return hookSetting.isBeforeUpdate();
                case AFTER_UPDATE:
                    return hookSetting.isAfterUpdate();
                case BEFORE_DELETE:
                    return hookSetting.isBeforeDelete();
                case AFTER_DELETE:
                    return hookSetting.isAfterDelete();
                default:
                    return false;
            }
        }
    }

}
