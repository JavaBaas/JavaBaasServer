package com.javabaas.server.push.entity;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by Codi on 15/11/2.
 */
public class Push {

    // 透传信息，静默推送
    private PushMessage message;
    // 推送信息，可打通知栏
    private PushNotification notification;

    public PushMessage getMessage() {
        return message;
    }

    public void setMessage(PushMessage message) {
        this.message = message;
    }

    public PushNotification getNotification() {
        return notification;
    }

    public void setNotification(PushNotification notification) {
        this.notification = notification;
    }
}
