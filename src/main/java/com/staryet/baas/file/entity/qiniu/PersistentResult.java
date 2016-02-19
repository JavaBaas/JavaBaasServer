package com.staryet.baas.file.entity.qiniu;

import java.util.List;

/**
 * Created by Codi on 15/11/12.
 */
public class PersistentResult {
    private String id;
    private String inputKey;
    private int code;
    private List<PersistentItem> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInputKey() {
        return inputKey;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<PersistentItem> getItems() {
        return items;
    }

    public void setItems(List<PersistentItem> items) {
        this.items = items;
    }
}
