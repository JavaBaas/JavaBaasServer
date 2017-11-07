package com.javabaas.server.push.service;

import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.push.entity.Push;
import com.javabaas.server.push.entity.PushLog;
import com.javabaas.server.push.handler.IPushHandler;
import com.javabaas.server.user.service.InstallationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 推送
 * Created by Codi on 15/11/2.
 */
@Service
public class PushService {

    public static String PUSH_LOG_CLASS_NAME = "_PushLog";
    private Log logger = LogFactory.getLog(getClass());
    @Autowired
    private ObjectService objectService;

    @Autowired
    private Map<String, IPushHandler> handlers;
    @Autowired
    private AppConfigService appConfigService;

    public void sendPush(String appId, String plat, BaasQuery query, Push push) {
        String handlerName = appConfigService.getString(appId, AppConfigEnum.PUSH_HANDLER);
        IPushHandler pushHandler = handlers.get(handlerName);
        if (query == null) {
            //全体推送
            pushHandler.pushAll(appId, push);
        } else {
            //按查询条件推送
            List<BaasObject> devices = objectService.find(appId, plat, InstallationService.INSTALLATION_CLASS_NAME, query, null, null,
                    null, 1000, 0, null, true);
            Collection<String> ids = new LinkedList<>();
            devices.forEach(device -> ids.add(device.getId()));
            pushHandler.pushMulti(appId, ids, push);
        }
        PushLog pushLog = new PushLog();
        //记录推送日志
        if (push.getNotification() != null) {
            pushLog.setPushType(1);
            pushLog.setTitle(push.getNotification().getTitle());
            pushLog.setAlert(push.getNotification().getAlert());
            if (push.getNotification().getBadge() != 0) {
                pushLog.setBadge(push.getNotification().getBadge());
            }
            pushLog.setSound(push.getNotification().getSound());
        } else if (push.getMessage() != null) {
            pushLog.setPushType(2);
            pushLog.setTitle(push.getMessage().getTitle());
            pushLog.setAlert(push.getMessage().getAlert());
            pushLog.setContentType(push.getMessage().getContentType());
            if (push.getMessage().getExtras() != null) {
                pushLog.setParams(push.getMessage().getExtras());
            }
        }
        pushLog.setPushTime(new Date().getTime());
        if (query != null) {
            pushLog.setWhere(query);
        }
        pushLog = new PushLog(objectService.insert(appId, plat, PUSH_LOG_CLASS_NAME, pushLog, true, null, true));
        logger.debug("App:" + appId
                + " 推送成功 id:" + pushLog.getId()
                + " title:" + pushLog.getTitle()
                + " alert:" + pushLog.getAlert()
                + " badge:" + pushLog.getBadge()
                + " sound:" + pushLog.getSound()
                + " where:" + pushLog.getWhere());
    }
}
