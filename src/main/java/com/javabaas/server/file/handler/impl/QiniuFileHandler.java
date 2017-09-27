package com.javabaas.server.file.handler.impl;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.config.BaasConfig;
import com.javabaas.server.file.entity.BaasFile;
import com.javabaas.server.file.entity.qiniu.PersistentResult;
import com.javabaas.server.file.entity.qiniu.QiniuConfig;
import com.javabaas.server.file.handler.IFileHandler;
import com.javabaas.server.file.service.FileService;
import com.javabaas.server.object.entity.BaasList;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.object.service.ObjectService;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.processing.OperationManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FetchRet;
import com.qiniu.util.Auth;
import com.qiniu.util.Base64;
import com.qiniu.util.StringMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 七牛文件处理
 * Created by Staryet on 15/8/27.
 */
@Component
public class QiniuFileHandler implements IFileHandler {

    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private FileService fileService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private JSONUtil jsonUtil;

    @Autowired
    private QiniuConfig qiniuConfig;
    @Autowired
    private BaasConfig baasConfig;

    @Override
    public Map<String, Object> getToken(String appId, String plat, String name, Map<String, Object> policy) {
        String callbackUrl = baasConfig.getHost() + "api/file/callback";
        String notifyUrl = baasConfig.getHost() + "api/file/notify/qiniu";
        Auth auth = Auth.create(qiniuConfig.getAk(), qiniuConfig.getSk());
        //获取文件唯一key
        String key = fileService.getFileKey();
        //文件名为 appId/key
        String fileName = appId + "/" + key;
        //生成上传策略
        StringMap policyMap = new StringMap();
        //添加客户端上传的额外策略
        if (policy != null) {
            String persistentOps = (String) policy.get("persistentOps");
            if (!StringUtils.isEmpty(persistentOps)) {
                //持久化处理
                String[] ops = persistentOps.split(";");
                StringBuilder persistent = new StringBuilder();
                for (String op : ops) {
                    //处理多个策略并发的情况 为每一个处理策略添加文件存储地址
                    String persistentFileKey = fileService.getFileKey();
                    String persistentFileName = appId + "/" + persistentFileKey;
                    persistentFileKey = "saveas/" + new String(Base64.encode((qiniuConfig.getBucket() + ":" + persistentFileName)
                            .getBytes(), 0));
                    persistent.append(op).append("|").append(persistentFileKey).append(";");
                }
                policyMap.put("persistentOps", persistent.toString());
            }
        }
        policyMap.put("callbackUrl", callbackUrl);
        policyMap.put("persistentNotifyUrl", notifyUrl);
        if (!StringUtils.isEmpty(qiniuConfig.getPipeline())) {
            policyMap.put("persistentPipeline", qiniuConfig.getPipeline());
        }
        String url = "platform=qiniu&key=" + key + "&source=" + name + "&app=" + appId + "&plat=" + plat + "&mimeType=$(mimeType)&size=$" +
                "(fsize)&duration=$(avinfo.format.duration)&avinfo=$(avinfo)&persistentId=$(persistentId)";
        policyMap.put("callbackBody", url);
        policyMap.put("returnBody", url);
        String token = auth.uploadToken(qiniuConfig.getBucket(), fileName, 60000, policyMap);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("name", fileName);
        return result;
    }

    @Override
    public BaasFile callback(String body, HttpServletRequest request) {
        //云存储回调 处理BaasFile的存储
        Auth auth = Auth.create(qiniuConfig.getAk(), qiniuConfig.getSk());
        boolean isValid = auth.isValidCallback(request.getHeader("Authorization"), request.getRequestURL().toString(), body.getBytes(),
                request.getContentType());
        if (!isValid) {
            //授权失败 无效请求
            throw new SimpleError(SimpleCode.FILE_CALLBACK_NO_VALID);
        }
        //获取参数
        BaasFile file = new BaasFile();
        return getFileParams(file, body);
    }

    @Override
    public BaasFile process(String appId, String plat, String fileId, Map<String, Object> policy) {
        BaasObject object = objectService.get(appId, plat, FileService.FILE_CLASS_NAME, fileId);
        if (object == null) {
            //文件不存在
            throw new SimpleError(SimpleCode.FILE_NOT_EXIST);
        }
        BaasFile file = new BaasFile(object);
        Auth auth = Auth.create(qiniuConfig.getAk(), qiniuConfig.getSk());
        OperationManager operationManager = new OperationManager(auth, new Configuration(Zone.zone1()));
        StringMap policyMap = new StringMap();
        policyMap.putAll(policy);
        String notifyUrl = baasConfig.getHost() + "api/file/notify/qiniu";
        policyMap.put("notifyURL", notifyUrl);
        if (!StringUtils.isEmpty(qiniuConfig.getPipeline())) {
            policyMap.put("persistentPipeline", qiniuConfig.getPipeline());
        }
        String fileKey = appId + "/" + file.getKey();
        try {
            String fops = (String) policy.get("persistentOps");
            String result = operationManager.pfop(qiniuConfig.getBucket(), fileKey, fops, policyMap);
            //将持久化id记录至持久化处理列表中
            BaasList persistentFiles = new BaasList();
            BaasObject persistentFile = new BaasObject();
            persistentFile.put("persistentId", result);
            persistentFiles.add(persistentFile);
            file.setPersistentFiles(persistentFiles);
            objectService.update(appId, plat, FileService.FILE_CLASS_NAME, fileId, file, null, true);
        } catch (QiniuException e) {
            //持久化处理失败
            log.error("七牛持久化处理失败 appId:" + appId + " fileId:" + fileId, e);
            throw new SimpleError(SimpleCode.FILE_PROCESS_FAILED);
        }
        return file;
    }

    @Override
    public void persistentNotify(String body, HttpServletRequest request) {
        //查找源文件
        PersistentResult persistentResult = jsonUtil.readValue(body, PersistentResult.class);
        String inputKey = persistentResult.getInputKey();
        String appId = inputKey.split("/")[0];
        String key = inputKey.split("/")[1];
        if (persistentResult.getCode() == 0) {
            //持久化处理成功
            BaasQuery query = new BaasQuery();
            query.put("key", key);
            List<BaasObject> files = objectService.find(appId, "admin", FileService.FILE_CLASS_NAME, query, null, null, null, 1, 0,
                    null, true);
            if (files.size() > 0) {
                //源文件
                BaasFile sourceFile = new BaasFile(files.get(0));
                //持久化列表
                BaasList persistentFiles = new BaasList();
                //循环七牛返回的持久化结果
                persistentResult.getItems().forEach(item -> {
                    BaasObject object = new BaasObject();
                    //存储持久化处理后的文件
                    BaasFile persistentFile = new BaasFile();
                    String persistentFileKey = item.getKey().split("/")[1];
                    persistentFile.setKey(persistentFileKey);
                    persistentFile.setUrl(qiniuConfig.getUrl() + item.getKey());
                    object.put("cmd", item.getCmd());
                    persistentFile = fileService.saveFile(appId, "admin", persistentFile);
                    object.put("_id", persistentFile.getId());
                    persistentFiles.add(object);
                });
                //更新源文件
                sourceFile.setPersistentFiles(persistentFiles);
                objectService.update(appId, "admin", FileService.FILE_CLASS_NAME, sourceFile.getId(), sourceFile, null, true);
            }
        } else {
            log.error("App:" + appId + " 持久化处理失败 fileKey:" + key);
            log.error("App:" + appId + body);
        }

    }

    @Override
    public BaasFile fetch(String appId, String plat, BaasFile file, Map<String, Object> policy) {
        try {
            Configuration cfg = new Configuration(Zone.zone1());
            Auth auth = Auth.create(qiniuConfig.getAk(), qiniuConfig.getSk());
            String bucket = qiniuConfig.getBucket();
            String key = fileService.getFileKey();
            String name = appId + "/" + key;
            BucketManager bucketManager = new BucketManager(auth, cfg);
            FetchRet fetchRet = bucketManager.fetch(file.getUrl(), bucket, name);
            file = getFetchFile(file, fetchRet, appId, plat);
            return file;
        } catch (QiniuException e) {
            log.error(e, e);
            throw new SimpleError(SimpleCode.FILE_FETCH_FAILED);
        }
    }

    private BaasFile getFetchFile(BaasFile file, FetchRet fetchRet, String appId, String plat) {
        String[] keys = fetchRet.key.split("/");
        file.setKey(keys[1]);
        file.setName("fetch");
        file.setMimeType(fetchRet.mimeType);
        file.setSize(fetchRet.fsize);
        file.setUrl(qiniuConfig.getUrl() + fetchRet.key);
        file = fileService.saveFile(appId, plat, file);
        return file;
    }

    private BaasFile getFileParams(BaasFile file, String paramsString) {
        try {
            paramsString = URLDecoder.decode(paramsString, "utf-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        //获取参数
        Map<String, String> params = getParams(paramsString);
        String key = params.get("key");
        file.setKey(key);
        file.setName(params.get("source"));
        file.setMimeType(params.get("mimeType"));
        file.setSize(Long.valueOf(params.get("size")));
        String plat = params.get("plat");
        String appId = params.get("app");
        file.setUrl(qiniuConfig.getUrl() + appId + "/" + key);
        //处理metaData
        if (params.get("duration") != null || params.get("avinfo") != null) {
            BaasObject metaData = new BaasObject();
            file.put("metaData", metaData);
            if (params.get("duration") != null) metaData.put("duration", params.get("duration"));
            if (params.get("avinfo") != null) metaData.put("avinfo", params.get("avinfo"));
        }
        //持久化处理结果
        if (params.get("persistentId") != null) {
            //记录至operate列表中
            BaasList persistentFiles = new BaasList();
            BaasObject persistentFile = new BaasObject();
            persistentFile.put("persistentId", params.get("persistentId"));
            persistentFiles.add(persistentFile);
            file.setPersistentFiles(persistentFiles);
        }
        file = fileService.saveFile(appId, plat, file);
        return file;
    }

    private Map<String, String> getParams(String body) {
        Map<String, String> params = new HashMap<>();
        String[] p = body.split("&");
        for (String param : p) {
            String[] kv = param.split("=");
            if (kv.length == 2) {
                String name = kv[0];
                String value = kv[1];
                params.put(name, value);
            }
        }
        return params;
    }

}
