package com.javabaas.server.cloud.entity;

/**
 * Created by Staryet on 15/9/23.
 */
public class HookSetting {

    private boolean beforeInsert;
    private boolean afterInsert;
    private boolean beforeUpdate;
    private boolean afterUpdate;
    private boolean beforeDelete;
    private boolean afterDelete;

    public HookSetting() {
    }

    public HookSetting(boolean enable) {
        this.beforeInsert = enable;
        this.afterInsert = enable;
        this.beforeUpdate = enable;
        this.afterUpdate = enable;
        this.beforeDelete = enable;
        this.afterDelete = enable;
    }

    public HookSetting(boolean beforeInsert, boolean afterInsert, boolean beforeUpdate, boolean afterUpdate, boolean beforeDelete, boolean afterDelete) {
        this.beforeInsert = beforeInsert;
        this.afterInsert = afterInsert;
        this.beforeUpdate = beforeUpdate;
        this.afterUpdate = afterUpdate;
        this.beforeDelete = beforeDelete;
        this.afterDelete = afterDelete;
    }

    public boolean isBeforeInsert() {
        return beforeInsert;
    }

    public void setBeforeInsert(boolean beforeInsert) {
        this.beforeInsert = beforeInsert;
    }

    public boolean isAfterInsert() {
        return afterInsert;
    }

    public void setAfterInsert(boolean afterInsert) {
        this.afterInsert = afterInsert;
    }

    public boolean isBeforeUpdate() {
        return beforeUpdate;
    }

    public void setBeforeUpdate(boolean beforeUpdate) {
        this.beforeUpdate = beforeUpdate;
    }

    public boolean isAfterUpdate() {
        return afterUpdate;
    }

    public void setAfterUpdate(boolean afterUpdate) {
        this.afterUpdate = afterUpdate;
    }

    public boolean isBeforeDelete() {
        return beforeDelete;
    }

    public void setBeforeDelete(boolean beforeDelete) {
        this.beforeDelete = beforeDelete;
    }

    public boolean isAfterDelete() {
        return afterDelete;
    }

    public void setAfterDelete(boolean afterDelete) {
        this.afterDelete = afterDelete;
    }
}
