package com.javabaas.server.push.entity;

import java.util.Map;

/**
 * Created by test on 2017/6/27.
 * <p>
 * 应用内消息。或者称作：自定义消息，透传消息。
 * 此部分内容不会展示到通知栏上。
 */
public class PushMessage {

    // 消息标题
    private String title;
    // 消息内容本身
    private String alert;
    // 消息内容类型, 例如text
    private String contentType;
    // 扩展参数
    private Map<String, String> extras;


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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "title='" + title + '\'' +
                ", alert='" + alert + '\'' +
                ", contentType='" + contentType + '\'' +
                ", extras=" + extras +
                '}';
    }
}
