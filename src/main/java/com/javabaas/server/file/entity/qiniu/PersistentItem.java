package com.javabaas.server.file.entity.qiniu;

/**
 * Created by Codi on 15/11/12.
 */
public class PersistentItem {

    private String cmd;
    private String key;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
