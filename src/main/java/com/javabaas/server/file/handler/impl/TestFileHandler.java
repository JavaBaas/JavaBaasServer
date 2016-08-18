package com.javabaas.server.file.handler.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.file.entity.BaasFile;
import com.javabaas.server.file.handler.IFileHandler;
import com.javabaas.server.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Codi on 16/1/10.
 */
@Component
public class TestFileHandler implements IFileHandler {

    @Autowired
    private FileService fileService;
    @Autowired
    private JSONUtil jsonUtil;

    @Override
    public Map<String, Object> getToken(String appId, String plat, String name, Map<String, Object> policy) {
        return null;
    }

    @Override
    public BaasFile fetch(String appId, String plat, BaasFile file, Map<String, Object> policy) {
        file = fileService.saveFile(appId, plat, file);
        return file;
    }

    @Override
    public BaasFile callback(String body, HttpServletRequest request) {
        BaasFile file = new BaasFile();
        Map<String, String> params = jsonUtil.readValue(body, new TypeReference<HashMap<String, String>>() {
        });
        file.setName(params.get("source"));
        String plat = params.get("plat");
        String appId = params.get("app");
        return fileService.saveFile(appId, plat, file);
    }

    @Override
    public BaasFile process(String appId, String plat, String fileId, Map<String, Object> policy) {
        return null;
    }

    @Override
    public void persistentNotify(String body, HttpServletRequest request) {

    }

}
