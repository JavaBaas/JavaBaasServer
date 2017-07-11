package com.javabaas.server.config.entity;

import org.springframework.util.StringUtils;

/**
 * Created by Codi on 2017/7/6.
 */
public enum AppConfigEnum {
    SMS_TRY_LIMIT("baas.sms.tryLimit", "重试次数", "5"),
    SMS_HANDLER("baas.sms.handler", "短信发送器", "aliyun"),
    SMS_HANDLER_ALIYUN_KEY("baas.sms.handler.aliyun.key", "阿里云key", ""),
    SMS_HANDLER_ALIYUN_SECRET("baas.sms.handler.aliyun.secret", "阿里云secret", ""),
    SMS_CODE_TEMPLATE_ID("baas.sms.codeTemplateId", "短信验证码模版id", ""),
    SMS_SIGN_NAME("baas.sms.signName", "短信签名", ""),
    SMS_SEND_INTERVAL("baas.sms.interval", "短信发送间隔", "60");

    private String key;
    private String name;
    private String defaultValue;

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public static AppConfigEnum getConfig(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        } else {
            for (AppConfigEnum appConfigEnum : AppConfigEnum.values()) {
                if (key.equals(appConfigEnum.getKey())) {
                    return appConfigEnum;
                }
            }
        }
        return null;
    }

    public static String getDefaultValue(String key) {
        AppConfigEnum config = getConfig(key);
        if (config != null) {
            return config.getDefaultValue();
        } else {
            return null;
        }
    }

    AppConfigEnum(String key, String name, String defaultValue) {
        this.key = key;
        this.name = name;
        this.defaultValue = defaultValue;
    }
}
