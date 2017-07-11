package com.javabaas.server.admin.service;

import com.javabaas.server.admin.entity.*;
import com.javabaas.server.admin.entity.dto.AppExport;
import com.javabaas.server.admin.entity.dto.ClazzExport;
import com.javabaas.server.admin.repository.AppRepository;
import com.javabaas.server.cloud.entity.CloudSetting;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.config.service.AppConfigService;
import com.javabaas.server.file.service.FileService;
import com.javabaas.server.object.dao.impl.mongo.MongoDao;
import com.javabaas.server.push.service.PushService;
import com.javabaas.server.sms.service.SmsService;
import com.javabaas.server.user.service.InstallationService;
import com.javabaas.server.user.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 应用服务
 * Created by Staryet on 15/9/15.
 */
@Service
public class AppService {

    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private AppConfigService appConfigService;
    @Autowired
    private MongoDao dao;
    @Autowired
    private JSONUtil jsonUtil;

    public App insert(App app) {
        String name = app.getName();
        if (StringUtils.isEmpty(name)) {
            throw new SimpleError(SimpleCode.APP_NAME_ERROR);
        }
        if (!isNameValid(name)) {
            throw new SimpleError(SimpleCode.APP_NAME_ERROR);
        }
        App exist = appRepository.findByName(name);
        if (exist != null) {
            throw new SimpleError(SimpleCode.APP_ALREADY_EXIST);
        } else {
            app.setId(null);
            if (StringUtils.isEmpty(app.getKey())) {
                //初始化key masterKey
                app.setKey(getKey());
                app.setMasterKey(getKey());
                //存储
                appRepository.insert(app);
                //初始化应用
                init(app.getId());
            } else {
                //导入应用 只存储 不做其他操作
                appRepository.insert(app);
            }
        }
        return app;
    }

    public void deleteByAppName(String name) {
        App app = appRepository.findByName(name);
        if (app != null) {
            delete(app.getId());
        }
    }

    public void delete(String id) {
        //删除包含的类
        clazzService.deleteAll(id);
        //删除配置
        appConfigService.deleteConfig(id);
        //删除自己
        appRepository.delete(id);
        //删除缓存
        deleteCache(id);
        //删除DB
        dao.removeApp(id);
    }

    public App get(String id) {
        App app = getCache(id);
        if (app == null) {
            //未找到缓存
            app = appRepository.findOne(id);
            if (app == null) {
                throw new SimpleError(SimpleCode.APP_NOT_FOUND);
            }
            setCache(id, app);
        }
        return app;
    }

    public void setCloudSetting(String id, CloudSetting setting) {
        App app = get(id);
        app.setCloudSetting(setting);
        appRepository.save(app);
        //清除缓存信息
        deleteCache(id);
    }

    public void setAccount(String id, AccountType accountType, Account account) {
        App app = get(id);
        if (app.getAppAccounts() == null) {
            app.setAppAccounts(new AppAccounts());
        }
        app.getAppAccounts().setAccount(accountType, account);
        appRepository.save(app);
        //清除缓存信息
        deleteCache(id);
    }

    public List<App> list() {
        return appRepository.findAll();
    }

    public void resetKey(String id) {
        App exist = get(id);
        exist.setKey(getKey());
        appRepository.save(exist);
        deleteCache(id);
    }

    public void resetMasterKey(String id) {
        App exist = get(id);
        exist.setMasterKey(getKey());
        appRepository.save(exist);
        deleteCache(id);
    }

    public AppExport export(String id) {
        App app = get(id);
        AppExport appExport = new AppExport();
        BeanUtils.copyProperties(app, appExport);
        //获取包含的类信息
        appExport.setClazzs(clazzService.export(id));
        return appExport;
    }

    public App importData(AppExport appExport) {
        //导入应用信息
        App app = new App();
        BeanUtils.copyProperties(appExport, app);
        insert(app);
        //导入类信息
        List<ClazzExport> clazzs = appExport.getClazzs();
        for (ClazzExport clazzExport : clazzs) {
            clazzService.importData(app.getId(), clazzExport);
        }
        return app;
    }

    /**
     * 初始化应用
     *
     * @param appId 应用id
     * @throws SimpleError
     */
    private void init(String appId) {
        //初始化内建类
        //初始化用户类
        initUserClass(appId);
        //初始化设备类
        initInstallationClass(appId);
        //初始化文件类
        initFileClass(appId);
        //初始化推送日志类
        initPushLogClass(appId);
        //初始化短信日志类
        initSmsLogClass(appId);
        log.info("App:" + appId + " 应用初始化成功.");
    }

    private void initSmsLogClass(String appId) {
        Clazz smsLogClazz = new Clazz();
        smsLogClazz.setName(SmsService.SMS_LOG_CLASS_NAME);
        smsLogClazz.setInternal(true);
        clazzService.insert(appId, smsLogClazz, false);
        //手机号
        Field phoneField = new Field(FieldType.STRING, "phone");
        phoneField.setInternal(true);
        fieldService.insert(appId, SmsService.SMS_LOG_CLASS_NAME, phoneField);
        //短信签名
        Field signName = new Field(FieldType.STRING, "signName");
        signName.setInternal(true);
        fieldService.insert(appId, SmsService.SMS_LOG_CLASS_NAME, signName);
        //模版编号
        Field templateId = new Field(FieldType.STRING, "templateId");
        templateId.setInternal(true);
        fieldService.insert(appId, SmsService.SMS_LOG_CLASS_NAME, templateId);
        //发送参数
        Field params = new Field(FieldType.OBJECT, "params");
        templateId.setInternal(true);
        fieldService.insert(appId, SmsService.SMS_LOG_CLASS_NAME, params);
        //发送状态
        Field state = new Field(FieldType.NUMBER, "state");
        state.setInternal(true);
        fieldService.insert(appId, SmsService.SMS_LOG_CLASS_NAME, state);
        log.info("App:" + appId + " 短信日志类初始化成功.");
    }

    private void initPushLogClass(String appId) {
        Clazz pushLogClazz = new Clazz();
        pushLogClazz.setName(PushService.PUSH_LOG_CLASS_NAME);
        pushLogClazz.setInternal(true);
        clazzService.insert(appId, pushLogClazz, false);
        //推送标题
        Field titleField = new Field(FieldType.STRING, "title");
        titleField.setInternal(true);
        fieldService.insert(appId, PushService.PUSH_LOG_CLASS_NAME, titleField);
        //推送内容
        Field alertField = new Field(FieldType.STRING, "alert");
        alertField.setInternal(true);
        fieldService.insert(appId, PushService.PUSH_LOG_CLASS_NAME, alertField);
        //推送圆标个数
        Field badgeField = new Field(FieldType.NUMBER, "badge");
        badgeField.setInternal(true);
        fieldService.insert(appId, PushService.PUSH_LOG_CLASS_NAME, badgeField);
        //推送声音
        Field soundField = new Field(FieldType.STRING, "sound");
        soundField.setInternal(true);
        fieldService.insert(appId, PushService.PUSH_LOG_CLASS_NAME, soundField);
        //推送条件
        Field whereField = new Field(FieldType.OBJECT, "where");
        whereField.setInternal(true);
        fieldService.insert(appId, PushService.PUSH_LOG_CLASS_NAME, whereField);
        //推送参数
        Field paramsField = new Field(FieldType.OBJECT, "params");
        paramsField.setInternal(true);
        fieldService.insert(appId, PushService.PUSH_LOG_CLASS_NAME, paramsField);
        //推送时间
        Field pushTimeField = new Field(FieldType.DATE, "pushTime");
        pushTimeField.setInternal(true);
        fieldService.insert(appId, PushService.PUSH_LOG_CLASS_NAME, pushTimeField);
        log.info("App:" + appId + " 推送日志类初始化成功.");
    }

    private void initFileClass(String appId) {
        Clazz fileClazz = new Clazz();
        fileClazz.setName(FileService.FILE_CLASS_NAME);
        fileClazz.setInternal(true);
        clazzService.insert(appId, fileClazz, false);
        //初始化文件类字段
        //文件名
        Field nameField = new Field(FieldType.STRING, "name");
        nameField.setInternal(true);
        fieldService.insert(appId, FileService.FILE_CLASS_NAME, nameField);
        //地址
        Field urlField = new Field(FieldType.STRING, "url");
        urlField.setInternal(true);
        fieldService.insert(appId, FileService.FILE_CLASS_NAME, urlField);
        //键
        Field keyField = new Field(FieldType.STRING, "key");
        keyField.setInternal(true);
        fieldService.insert(appId, FileService.FILE_CLASS_NAME, keyField);
        //类型
        Field mimeTypeField = new Field(FieldType.STRING, "mimeType");
        mimeTypeField.setInternal(true);
        fieldService.insert(appId, FileService.FILE_CLASS_NAME, mimeTypeField);
        //尺寸
        Field sizeField = new Field(FieldType.NUMBER, "size");
        sizeField.setInternal(true);
        fieldService.insert(appId, FileService.FILE_CLASS_NAME, sizeField);
        //附加信息
        Field metaDataField = new Field(FieldType.OBJECT, "metaData");
        metaDataField.setInternal(true);
        fieldService.insert(appId, FileService.FILE_CLASS_NAME, metaDataField);
        //持久化处理列表
        Field persistentFilesField = new Field(FieldType.ARRAY, "persistentFiles");
        persistentFilesField.setInternal(true);
        fieldService.insert(appId, FileService.FILE_CLASS_NAME, persistentFilesField);
        log.info("App:" + appId + " 文件类初始化成功.");
    }

    private void initInstallationClass(String appId) {
        Clazz installationClazz = new Clazz();
        installationClazz.setName(InstallationService.INSTALLATION_CLASS_NAME);
        installationClazz.setInternal(true);
        clazzService.insert(appId, installationClazz, false);
        //初始化设备类字段
        Field installationId = new Field(FieldType.STRING, "installationId");
        installationId.setInternal(true);
        fieldService.insert(appId, InstallationService.INSTALLATION_CLASS_NAME, installationId);
        Field deviceToken = new Field(FieldType.STRING, "deviceToken");
        deviceToken.setInternal(true);
        fieldService.insert(appId, InstallationService.INSTALLATION_CLASS_NAME, deviceToken);
        Field deviceType = new Field(FieldType.STRING, "deviceType");
        deviceType.setInternal(true);
        fieldService.insert(appId, InstallationService.INSTALLATION_CLASS_NAME, deviceType);
        log.info("App:" + appId + " 设备类初始化成功.");
    }

    private void initUserClass(String appId) {
        Clazz clazzUser = new Clazz();
        clazzUser.setName(UserService.USER_CLASS_NAME);
        clazzUser.setInternal(true);
        clazzService.insert(appId, clazzUser, false);
        //初始化用户类字段
        Field username = new Field(FieldType.STRING, "username");
        username.setInternal(true);
        fieldService.insert(appId, UserService.USER_CLASS_NAME, username);
        Field password = new Field(FieldType.STRING, "password");
        password.setInternal(true);
        password.setSecurity(true);
        fieldService.insert(appId, UserService.USER_CLASS_NAME, password);
        Field email = new Field(FieldType.STRING, "email");
        email.setInternal(true);
        fieldService.insert(appId, UserService.USER_CLASS_NAME, email);
        Field phone = new Field(FieldType.STRING, "phone");
        phone.setInternal(true);
        fieldService.insert(appId, UserService.USER_CLASS_NAME, phone);
        Field sessionToken = new Field(FieldType.STRING, "sessionToken");
        sessionToken.setInternal(true);
        sessionToken.setSecurity(true);
        fieldService.insert(appId, UserService.USER_CLASS_NAME, sessionToken);
        Field auth = new Field(FieldType.OBJECT, "auth");
        auth.setInternal(true);
        auth.setSecurity(true);
        fieldService.insert(appId, UserService.USER_CLASS_NAME, auth);
        log.info("App:" + appId + " 用户类初始化成功.");
    }

    private String getKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private App getCache(String appId) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String string = ops.get("App_" + appId);
        return jsonUtil.readValue(string, App.class);
    }

    private void setCache(String appId, App app) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("App_" + appId, jsonUtil.writeValueAsString(app));
    }

    private void deleteCache(String appId) {
        redisTemplate.delete("App_" + appId);
        log.debug("App:" + appId + " 缓存已清除");
    }


    /**
     * 检查名称是否合法 (字母开头，只包含字母数字下划线)
     *
     * @param name 名称
     * @return 是否合法
     */
    private boolean isNameValid(String name) {
        String regex = "^[a-zA-Z][a-zA-Z0-9_]*$";
        return Pattern.matches(regex, name);
    }

}
