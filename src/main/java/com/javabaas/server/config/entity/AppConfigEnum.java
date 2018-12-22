package com.javabaas.server.config.entity;

import org.springframework.util.StringUtils;

/**
 * Created by Codi on 2017/7/6.
 */
public enum AppConfigEnum {
    // 短信相关
    SMS_TRY_LIMIT("baas.sms.tryLimit", "重试次数", "5"),
    SMS_HANDLER("baas.sms.handler", "短信发送器", "aliyun"),
    SMS_HANDLER_ALIYUN_KEY("baas.sms.handler.aliyun.key", "阿里云key", ""),
    SMS_HANDLER_ALIYUN_SECRET("baas.sms.handler.aliyun.secret", "阿里云secret", ""),
    SMS_CODE_TEMPLATE_ID("baas.sms.codeTemplateId", "短信验证码模版id", ""),
    SMS_REGISTER_TEMPLATE_ID("baas.sms.registerTemplateId", "登录注册验证码模版id", ""),
    SMS_BIND_TEMPLATE_ID("baas.sms.bindTemplateId", "绑定手机号验证码模版id", ""),
    SMS_SIGN_NAME("baas.sms.signName", "短信签名", ""),
    SMS_SEND_INTERVAL("baas.sms.interval", "短信发送间隔", "60"),
    SMS_REGISTER_SUPER_CODE("baas.sms.register.super.code", "登录注册万能验证码",""),
    // 推送相关
    PUSH_HANDLER("baas.push.handler", "推送处理", "jpush"),
    PUSH_HANDLER_JPUSH_KEY("baas.push.handler.jpush.key", "极光推送key", ""),
    PUSH_HANDLER_JPUSH_SECRET("baas.push.handler.jpush.secret", "极光推送secret", ""),
    // 文件存储相关
    FILE_HANDLER("baas.file.handler", "推送处理", "qiniu"),
    FILE_HANDLER_QINIU_AK("baas.file.handler.qiniu.ak", "七牛ak", ""),
    FILE_HANDLER_QINIU_SK("baas.file.handler.qiniu.sk", "七牛sk", ""),
    FILE_HANDLER_QINIU_BUCKET("baas.file.handler.qiniu.bucket", "七牛bucket", ""),
    FILE_HANDLER_QINIU_PIPELINE("baas.file.handler.qiniu.pipeline", "七牛pipeline", ""),
    FILE_HANDLER_QINIU_URL("baas.file.handler.qiniu.url", "七牛url", ""),
    ///////// huadong/huabei/huanan/beimei/auto   一共四个zone，如果无值或者值没有匹配项，则认为是auto
    FILE_HANDLER_QINIU_ZONE("baas.file.handler.qiniu.zone", "七牛zone", ""),
    // 微信小程序
    WEBAPP_APPID("baas.webapp.appid", "微信小程序appid", ""),
    WEBAPP_SECRET("baas.webapp.secret", "微信小程序secret", "");

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
