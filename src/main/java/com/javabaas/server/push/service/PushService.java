package com.javabaas.server.push.service;

import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.push.entity.Push;
import com.javabaas.server.push.entity.PushLog;
import com.javabaas.server.push.handler.IPushHandler;
import com.javabaas.server.push.handler.impl.JPushHandler;
import com.javabaas.server.user.service.InstallationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 推送
 * Created by Codi on 15/11/2.
 */
@Service
public class PushService {

    public static String PUSH_LOG_CLASS_NAME = "_PushLog";
    private Log logger = LogFactory.getLog(getClass());
    @Autowired
    private AppService appService;
    @Autowired
    private ObjectService objectService;
    @Resource(type = JPushHandler.class)
    private IPushHandler pushHandler;

    public void sendPush(String appId, String plat, BaasQuery query, Push push) {
        if (query == null) {
            //全体推送
            pushHandler.pushAll(appId, push);
        } else {
            //按查询条件推送
            List<BaasObject> devices = objectService.list(appId, plat, InstallationService.INSTALLATION_CLASS_NAME, query, null, null, 1000, 0, null, true);
            Collection<String> ids = new LinkedList<>();
            devices.forEach(device -> ids.add(device.getId()));
            pushHandler.pushMulti(appId, ids, push);
        }
        //记录推送日志
        if (push.getNotification() != null) {
            PushLog pushLog = new PushLog();
            pushLog.setTitle(push.getNotification().getTitle());
            pushLog.setAlert(push.getNotification().getAlert());
            if (push.getNotification().getBadge() != 0) {
                pushLog.setBadge(push.getNotification().getBadge());
            }
            pushLog.setSound(push.getNotification().getSound());
            pushLog.setWhere(query);
            pushLog.setPushTime(new Date().getTime());
            pushLog = new PushLog(objectService.insert(appId, plat, PUSH_LOG_CLASS_NAME, pushLog, null, true));
            logger.debug("App:" + appId
                    + " 推送成功 id:" + pushLog.getId()
                    + " title:" + pushLog.getTitle()
                    + " alert:" + pushLog.getAlert()
                    + " badge:" + pushLog.getBadge()
                    + " sound:" + pushLog.getSound()
                    + " where:" + pushLog.getWhere());
        } else if (push.getMessage() != null) {
            //TODO 加透传日志
        }
    }
}
