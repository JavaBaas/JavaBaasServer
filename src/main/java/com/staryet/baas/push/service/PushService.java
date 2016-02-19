package com.staryet.baas.push.service;

import com.staryet.baas.admin.service.AppService;
import com.staryet.baas.object.entity.BaasObject;
import com.staryet.baas.object.entity.BaasQuery;
import com.staryet.baas.object.service.ObjectService;
import com.staryet.baas.push.entity.Push;
import com.staryet.baas.push.entity.PushAccount;
import com.staryet.baas.push.entity.PushLog;
import com.staryet.baas.push.handler.IPushHandler;
import com.staryet.baas.push.handler.impl.JPushHandler;
import com.staryet.baas.user.service.InstallationService;
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
        PushLog pushLog = new PushLog();
        pushLog.setTitle(push.getTitle());
        pushLog.setAlert(push.getAlert());
        if (push.getBadge() != null) {
            pushLog.setBadge(push.getBadge());
        }
        pushLog.setSound(push.getSound());
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
    }

    /**
     * 查询推送账号信息
     *
     * @param appId 应用id
     * @return 推送账号信息
     */
    public PushAccount getPushAccount(String appId) {
        return appService.get(appId).getPushAccount();
    }

    public void setPushAccount(String appId, PushAccount pushAccount) {
        appService.setPushAccount(appId, pushAccount);
    }
}
