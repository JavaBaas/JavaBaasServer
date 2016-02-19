package com.staryet.baas.hook.entity;

/**
 * Created by Codi on 15/10/11.
 */
public enum HookEvent {
    BEFORE_INSERT(1, "beforeInsert"),
    AFTER_INSERT(2, "afterInsert"),
    BEFORE_UPDATE(3, "beforeUpdate"),
    AFTER_UPDATE(4, "afterUpdate"),
    BEFORE_DELETE(5, "beforeDelete"),
    AFTER_DELETE(6, "afterDelete");

    private int code;
    private String name;

    HookEvent(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
