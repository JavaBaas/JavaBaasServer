package com.javabaas.server.file.service;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.file.entity.BaasFile;
import com.javabaas.server.file.entity.FileStoragePlatform;
import com.javabaas.server.file.handler.IFileHandler;
import com.javabaas.server.file.handler.impl.QiniuFileHandler;
import com.javabaas.server.file.handler.impl.TestFileHandler;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

/**
 * 文件存储服务
 * Created by Staryet on 15/8/26.
 */
@Service
public class FileService {

    public static String FILE_CLASS_NAME = "_File";
    @Autowired
    private ObjectService objectService;
    @Autowired
    private TestFileHandler testFileHandler;
    @Autowired
    private QiniuFileHandler qiniuFileHandler;

    public Map<String, Object> getToken(String appId, String plat, FileStoragePlatform filePlat, String fileName, Map<String, Object>
            policy) {
        return getFileHandler(filePlat).getToken(appId, plat, fileName, policy);
    }

    public BaasFile callback(FileStoragePlatform plat, String body, HttpServletRequest request) {
        return getFileHandler(plat).callback(body, request);
    }

    public void persistentNotify(FileStoragePlatform plat, String body, HttpServletRequest request) {
        getFileHandler(plat).persistentNotify(body, request);
    }

    public BaasFile getFile(String appId, String plat, String id) {
        BaasObject object = objectService.get(appId, plat, FILE_CLASS_NAME, id);
        if (object == null) {
            return null;
        } else {
            return new BaasFile(object);
        }
    }

    public BaasFile saveFile(String appId, String plat, BaasFile file) {
        //禁止设置ACL字段
        file.remove("acl");
        if (StringUtils.isEmpty(file.getKey())) {
            file.setKey(getFileKey());
        }
        return new BaasFile(objectService.insert(appId, plat, FILE_CLASS_NAME, file, true, null, true));
    }

    public BaasFile saveFileWithFetch(String appId, String plat, FileStoragePlatform platform, BaasFile file, Map<String, Object> policy) {
        //上传
        IFileHandler fileHandler = getFileHandler(platform);
        file = fileHandler.fetch(appId, plat, file, policy);
        return file;
    }

    public BaasFile process(String appId, String plat, FileStoragePlatform filePlat, String fileId, Map<String, Object> policy) {
        return getFileHandler(filePlat).process(appId, plat, fileId, policy);
    }

    private IFileHandler getFileHandler(FileStoragePlatform plat) {
        IFileHandler handler = null;
        switch (plat) {
            case Test:
                handler = testFileHandler;
                break;
            case Qiniu:
                handler = qiniuFileHandler;
                break;
            case Upyun:
                break;
            default:
                break;
        }
        if (handler == null) {
            throw new SimpleError(SimpleCode.FILE_NO_HANDLER);
        }
        return handler;
    }

    public String getFileKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
