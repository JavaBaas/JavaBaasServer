package com.javabaas.server.push.entity;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by Codi on 15/11/2.
 */
public class Push {

    private String title;
    @NotNull(message = "alert不能为空")
    private String alert;
    private Integer badge;
    private String sound;
    private Map<String, String> params;

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

    public Integer getBadge() {
        return badge;
    }

    public void setBadge(Integer badge) {
        this.badge = badge;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
