package com.javabaas.server.file.handler;

import com.javabaas.server.file.entity.BaasFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Staryet on 15/8/27.
 */
public interface IFileHandler {

    Map<String, Object> getToken(String appId, String plat, String name, Map<String, Object> policy);

    BaasFile fetch(String appId, String plat, BaasFile file, Map<String, Object> policy);

    BaasFile callback(String body, HttpServletRequest request);

    BaasFile process(String appId, String plat, String fileId, Map<String, Object> policy);

    void persistentNotify(String body, HttpServletRequest request);

}
