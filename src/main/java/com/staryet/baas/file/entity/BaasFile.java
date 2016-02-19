package com.staryet.baas.file.entity;

import com.staryet.baas.object.entity.BaasList;
import com.staryet.baas.object.entity.BaasObject;

import java.util.Map;

/**
 * Created by Staryet on 15/8/13.
 */
public class BaasFile extends BaasObject {

    public BaasFile() {
    }

    public BaasFile(Map<String, Object> m) {
        super(m);
    }

    public void setUrl(String url) {
        put("url", url);
    }

    public String getUrl() {
        return getString("url");
    }

    public void setName(String name) {
        put("name", name);
    }

    public String getName() {
        return getString("name");
    }

    public void setKey(String key) {
        put("key", key);
    }

    public String getKey() {
        return getString("key");
    }

    public void setMimeType(String mimeType) {
        put("mimeType", mimeType);
    }

    public String getMimeType() {
        return getString("mimeType");
    }

    public void setSize(long size) {
        put("size", size);
    }

    public long getSize() {
        return getLong("size");
    }

    public void setPersistentFiles(BaasList list) {
        put("persistentFiles", list);
    }

    public BaasList getPersistentFiles() {
        return getList("persistentFiles");
    }

}
