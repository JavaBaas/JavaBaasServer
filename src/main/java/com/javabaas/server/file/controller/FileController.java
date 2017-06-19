package com.javabaas.server.file.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.file.entity.BaasFile;
import com.javabaas.server.file.entity.FileStoragePlatform;
import com.javabaas.server.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Staryet on 15/8/13.
 */
@RestController
@RequestMapping(value = "/api")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private JSONUtil jsonUtil;

    /**
     * 创建文件
     *
     * @param body
     * @return
     * @throws SimpleError
     */
    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult insert(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @RequestParam(required = false) boolean fetch,
                               @RequestParam(required = false) String platform,
                               @RequestParam(required = false) String policy,
                               @RequestBody String body) {
        BaasFile file = jsonUtil.readValue(body, BaasFile.class);
        if (fetch) {
            FileStoragePlatform filePlat = FileStoragePlatform.get(platform);
            Map<String, Object> policyMap = null;
            if (!StringUtils.isEmpty(policy)) {
                policyMap = jsonUtil.readValue(policy, new TypeReference<HashMap<String, Object>>() {
                });
            }
            file = fileService.saveFileWithFetch(appId, plat, filePlat, file, policyMap);
        } else {
            file = fileService.saveFile(appId, plat, file);
        }
        SimpleResult result = SimpleResult.success();
        result.putData("file", file);
        return result;
    }

    /**
     * 客户端请求上传文件所需要的鉴权信息
     *
     * @param fileName 文件名
     * @param plat     平台
     * @return 令牌
     */
    @RequestMapping(value = "/file/getToken", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult getToken(@RequestHeader(value = "JB-AppId") String appId,
                                 @RequestHeader(value = "JB-Plat") String plat,
                                 @RequestParam(required = true) String fileName,
                                 @RequestParam(required = true) String platform,
                                 @RequestParam(required = false) String policy) {
        FileStoragePlatform filePlat = FileStoragePlatform.get(platform);
        SimpleResult result = SimpleResult.success();
        Map<String, Object> policyMap = null;
        if (!StringUtils.isEmpty(policy)) {
            policyMap = jsonUtil.readValue(policy, new TypeReference<HashMap<String, Object>>() {
            });
        }
        result.putDataAll(fileService.getToken(appId, plat, filePlat, fileName, policyMap));
        return result;
    }

    @RequestMapping(value = "/file/callback", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult callBack(@RequestBody String body, HttpServletRequest request) {
        SimpleResult result = SimpleResult.success();
        if (body.startsWith("platform=qiniu")) {
            //七牛的回调
            BaasFile file = fileService.callback(FileStoragePlatform.Qiniu, body, request);
            result.putData("file", file);
        } else {
            //无对应的文件处理器
            throw new SimpleError(SimpleCode.FILE_NO_HANDLER);
        }
        return result;
    }

    @RequestMapping(value = "/file/notify/qiniu", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult notify(@RequestBody String body, HttpServletRequest request) {
        //七牛的异步操作通知
        fileService.persistentNotify(FileStoragePlatform.Qiniu, body, request);
        return SimpleResult.success();
    }


    @RequestMapping(value = "/file/master/process", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult process(@RequestHeader(value = "JB-AppId") String appId,
                                @RequestHeader(value = "JB-Plat") String plat,
                                @RequestParam(required = true) String fileId,
                                @RequestParam(required = true) String platform,
                                @RequestParam(required = false) String policy) {
        FileStoragePlatform filePlat = FileStoragePlatform.get(platform);
        Map<String, Object> policyMap = null;
        if (!StringUtils.isEmpty(policy)) {
            policyMap = jsonUtil.readValue(policy, new TypeReference<HashMap<String, Object>>() {
            });
        }
        BaasFile file = fileService.process(appId, plat, filePlat, fileId, policyMap);
        SimpleResult result = SimpleResult.success();
        result.putData("file", file);
        return result;
    }

}
