package com.javabaas.server.push.entity;

import java.util.Map;

/**
 * Created by test on 2017/6/27.
 * <p>
 * 通知类消息，是会作为“通知”推送到客户端的
 */
public class PushNotification {
    // 标题
    private String title;
    // 内容 不能为空
    private String alert;
    // 通知提示音
    private String sound;
    // 应用角标
    private int badge;
    // 扩展参数
    private Map<String, String> extras;
    // ios静默推送选项
    private Boolean contentAvailable;
    // ios10支持的附件选项
    private Boolean mutableContent;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }

    public Boolean getContentAvailable() {
        return contentAvailable;
    }

    public void setContentAvailable(Boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
    }

    public Boolean getMutableContent() {
        return mutableContent;
    }

    public void setMutableContent(Boolean mutableContent) {
        this.mutableContent = mutableContent;
    }

    @Override
    public String toString() {
        return "PushNotification{" +
                "title='" + title + '\'' +
                ", alert='" + alert + '\'' +
                ", sound='" + sound + '\'' +
                ", badge=" + badge +
                ", extras=" + extras +
                ", contentAvailable=" + contentAvailable +
                ", mutableContent=" + mutableContent +
                '}';
    }
}
