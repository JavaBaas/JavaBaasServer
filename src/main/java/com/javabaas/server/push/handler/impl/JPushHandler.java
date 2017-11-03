package com.javabaas.server.push.handler.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import com.javabaas.server.push.entity.Push;
import com.javabaas.server.push.entity.PushMessage;
import com.javabaas.server.push.entity.PushNotification;
import com.javabaas.server.push.handler.IPushHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

/**
 * 极光推送
 * Created by Codi on 15/11/3.
 */
@Component("jpush")
public class JPushHandler implements IPushHandler {

    private Log logger = LogFactory.getLog(getClass());
    private Map<String, JPushClient> clients = new Hashtable<>();

    @Autowired
    private AppConfigService appConfigService;

    @Override
    public void pushSingle(String appId, String id, Push push) {
        pushMulti(appId, Collections.singletonList(id), push);
    }

    @Override
    public void pushMulti(String appId, Collection<String> ids, Push push) {
        PushPayload.Builder payloadBuilder = getPayloadBuilder(push);
        payloadBuilder.setAudience(Audience.newBuilder().addAudienceTarget(AudienceTarget.alias(ids)).build());
        payloadBuilder.setPlatform(Platform.all());
        push(appId, payloadBuilder);
    }

    @Override
    public void pushAll(String appId, Push push) {
        PushPayload.Builder payloadBuilder = getPayloadBuilder(push);
        payloadBuilder.setAudience(Audience.all());
        payloadBuilder.setPlatform(Platform.all());
        push(appId, payloadBuilder);
    }

    private void push(String appId, PushPayload.Builder payloadBuilder) {
        try {
            getPushClient(appId).sendPush(payloadBuilder.build());
        } catch (APIConnectionException e) {
            logger.debug("App:" + appId
                    + " 推送失败 无法连接服务器失败");
            throw new SimpleError(SimpleCode.PUSH_ERROR);
        } catch (APIRequestException e) {
            logger.debug("App:" + appId
                    + " 推送失败 错误信息:" + e.getErrorMessage());
            throw new SimpleError(SimpleCode.PUSH_ERROR);
        }
    }

    private PushPayload.Builder getPayloadBuilder(Push push) {
        //构建推送信息
        PushPayload.Builder builder = PushPayload.newBuilder();
        builder.setOptions(Options.newBuilder()
                .setApnsProduction(true)
                .build());

        //推送信息内容
        PushNotification notification = push.getNotification();
        if (notification != null) {
            IosNotification.Builder iosBuilder = IosNotification.newBuilder();
            iosBuilder.setAlert(notification.getAlert());
            if (notification.getBadge() != 0) {
                iosBuilder.setBadge(notification.getBadge());
            }
            if (push.getNotification().getSound() != null) {
                iosBuilder.setSound(notification.getSound());
            }
            AndroidNotification.Builder androidBuilder = AndroidNotification.newBuilder();
            androidBuilder.setAlert(notification.getAlert());
            androidBuilder.setTitle(notification.getTitle());
            if (notification.getExtras() != null) {
                //自定义消息
                iosBuilder.addExtras(notification.getExtras());
                androidBuilder.addExtras(notification.getExtras());
            }
            builder.setNotification(Notification.newBuilder()
                    .addPlatformNotification(iosBuilder.build())
                    .addPlatformNotification(androidBuilder.build())
                    .build());

        }
        //透传消息
        PushMessage message = push.getMessage();
        if (message != null) {
            Message.Builder messageBuilder = Message.newBuilder();
            messageBuilder.setTitle(message.getTitle());
            messageBuilder.setContentType(message.getContentType());
            messageBuilder.setMsgContent(message.getMsgContent());
            if (message.getExtras() != null) {
                messageBuilder.addExtras(message.getExtras());
            }
            builder.setMessage(messageBuilder.build());
        }
        return builder;
    }

    private JPushClient getPushClient(String appId) {
        JPushClient client = clients.get(appId);
        if (client == null) {
            client = new JPushClient(secret(appId), accessKey(appId));
            clients.put(appId, client);
        }
        return client;
    }

    private String accessKey(String appId) {
        return appConfigService.getString(appId, AppConfigEnum.PUSH_HANDLER_JPUSH_KEY);
    }

    private String secret(String appId) {
        return appConfigService.getString(appId, AppConfigEnum.PUSH_HANDLER_JPUSH_SECRET);
    }
}
