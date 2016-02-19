package com.staryet.baas.admin.entity.dto;

import com.staryet.baas.admin.entity.ClazzAcl;

import java.util.List;

/**
 * Created by Codi on 15/11/9.
 */
public class ClazzExport {

    private String id;
    private String name;
    private ClazzAcl acl;
    private boolean internal;
    private List<FieldExport> fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<FieldExport> getFields() {
        return fields;
    }

    public void setFields(List<FieldExport> fields) {
        this.fields = fields;
    }
}
