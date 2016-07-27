package com.javabaas.server.push.entity;

import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.object.entity.BaasObject;

import java.util.Map;

/**
 * 推送记录
 * Created by Codi on 15/11/2.
 */
public class PushLog extends BaasObject {

    public PushLog() {
    }

    public PushLog(Map<String, Object> m) {
        super(m);
    }

    public void setWhere(BaasQuery where) {
        put("where", where);
    }

    public BaasQuery getWhere() {
        return (BaasQuery) get("where");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getTitle() {
        return getString("title");
    }

    public void setAlert(String alert) {
        put("alert", alert);
    }

    public String getAlert() {
        return getString("alert");
    }

    public void setBadge(int badge) {
        put("badge", badge);
    }

    public Integer getBadge() {
        return getInt("badge");
    }

    public void setSound(String sound) {
        put("sound", sound);
    }

    public String getSound() {
        return getString("sound");
    }

    public void setParams(BaasObject params) {
        put("params", params);
    }

    public BaasObject getParams() {
        return (BaasObject) get("params");
    }

    public void setPushTime(long time) {
        put("pushTime", time);
    }

    public long getPushTime() {
        return getLong("pushTime");
    }

}
