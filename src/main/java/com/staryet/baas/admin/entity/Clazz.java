package com.staryet.baas.admin.entity;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Staryet on 15/6/5.
 */
@Document
public class Clazz {

    private String id;
    @DBRef
    private App app;
    private String name;
    private ClazzAcl acl;
    private boolean internal;
    private long count;

    public Clazz() {
    }

    public Clazz(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClazzAcl getAcl() {
        return acl;
    }

    public void setAcl(ClazzAcl acl) {
        this.acl = acl;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
