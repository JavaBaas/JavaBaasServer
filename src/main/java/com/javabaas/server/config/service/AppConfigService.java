package com.javabaas.server.config.service;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.config.entity.AppConfig;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.entity.AppConfigs;
import com.javabaas.server.config.repository.AppConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理应用相关配置
 * Created by Codi on 2017/7/6.
 */
@Service
public class AppConfigService {

    @Autowired
    private AppConfigRepository appConfigRepository;
    @Autowired
    private AppService appService;
    @Autowired
    private Environment env;
    private Map<String, String> configMap = new HashMap<>();

    public Object getConfig(String appId, AppConfigEnum config) {
        return getConfig(appId, config.getKey());
    }

    public String getString(String appId, AppConfigEnum config) {
        return String.valueOf(getConfig(appId, config));
    }

    public String getString(String appId, String key) {
        return String.valueOf(getConfig(appId, key));
    }

    public Long getLong(String appId, AppConfigEnum config) {
        return Long.valueOf(getString(appId, config));
    }

    public void setConfig(String appId, AppConfig appConfig) {
        checkConfigKey(appConfig.getKey());
        AppConfigs config = appConfigRepository.findByAppId(appId);
        if (config == null) {
            //配置不存在 创建配置
            config = new AppConfigs();
            App app = appService.get(appId);
            config.setApp(app);
        }
        config.setParam(appConfig);
        appConfigRepository.save(config);
        //清楚缓存
        clearCache(appId, appConfig.getKey());
    }

    public void setConfig(String appId, AppConfigEnum config, String value) {
        setConfig(appId, new AppConfig(config.getKey(), value));
    }

    private void checkConfigKey(String key) {
        if (AppConfigEnum.getConfig(key) == null) {
            throw new SimpleError(SimpleCode.APP_CONFIG_KEY_NOT_EXIST);
        }
    }

    public Object getConfig(String appId, String key) {
        checkConfigKey(key);
        //获取缓存的配置
        String config = configMap.get(getName(appId, key));
        if (StringUtils.isEmpty(config)) {
            //缓存中无配置 从数据库获取
            config = getConfigFromDB(appId, key);
            if (config == null) {
                //数据库无配置 从配置文件读取 配置文件为空时会自动配置为默认值
                config = getConfigFromProperty(key);
            }
        }
        //设置缓存
        if (!StringUtils.isEmpty(config)) {
            setCache(appId, key, config);
        }
        return config;
    }

    private String getConfigFromProperty(String key) {
        String property = env.getProperty(key);
        if (StringUtils.isEmpty(property)) {
            //未获取到设置值 获取默认值
            property = AppConfigEnum.getDefaultValue(key);
        }
        return property;
    }

    private String getConfigFromDB(String appId, String key) {
        AppConfigs appConfigs = appConfigRepository.findByAppId(appId);
        if (appConfigs == null) {
            return null;
        } else {
            return appConfigs.getParam(key.replaceAll("\\.", ""));
        }
    }

    private void setCache(String appId, String key, String value) {
        configMap.put(getName(appId, key), value);
    }

    private void clearCache(String appId, String key) {
        configMap.remove(getName(appId, key));
    }

    private String getName(String appId, String key) {
        return appId + "." + key;
    }

    public void deleteConfig(String appId) {
        appConfigRepository.deleteByAppId(appId);
    }

}
