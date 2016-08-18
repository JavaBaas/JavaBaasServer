package com.javabaas.server.admin.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Codi on 15/10/20.
 */
public class ApiStat {

    private String appId;
    private String plat;
    private String clazz;
    private ApiMethod method;
    private String date;

    public ApiStat(String appId, String plat, String clazz, ApiMethod method, String date) {
        this.appId = appId;
        this.plat = plat;
        this.clazz = clazz;
        this.method = method;
        this.date = date;
    }

    public ApiStat(String appId, String plat, String clazz, ApiMethod method, Date date) {
        this.appId = appId;
        this.plat = plat;
        this.clazz = clazz;
        this.method = method;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        this.date = simpleDateFormat.format(date);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public ApiMethod getMethod() {
        return method;
    }

    public void setMethod(ApiMethod method) {
        this.method = method;
    }

    public String getPlat() {
        return plat;
    }

    public void setPlat(String plat) {
        this.plat = plat;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "stat" + "_" + appId + "_" + plat + "_" + clazz + "_" + method.toString() + "_" + date;
    }
}
