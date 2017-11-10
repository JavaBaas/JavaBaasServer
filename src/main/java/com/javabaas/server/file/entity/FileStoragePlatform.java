package com.javabaas.server.file.entity;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import org.springframework.util.StringUtils;

/**
 * Created by Staryet on 15/8/27.
 */
public enum FileStoragePlatform {

    Test("test"),
    Qiniu("qiniu"),
    Upyun("upyun");

    private String name;

    FileStoragePlatform(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static FileStoragePlatform get(String name) {
        if (StringUtils.isEmpty(name)) {
            //未指定文件存储平台
            throw new SimpleError(SimpleCode.FILE_PLATFORM_EMPTY);
        }
        FileStoragePlatform[] plats = FileStoragePlatform.class.getEnumConstants();
        for (FileStoragePlatform plat : plats) {
            if (name.equals(plat.name)) {
                return plat;
            }
        }
        //文件存储平台错误
        throw new SimpleError(SimpleCode.FILE_PLATFORM_ERROR);
    }

}
