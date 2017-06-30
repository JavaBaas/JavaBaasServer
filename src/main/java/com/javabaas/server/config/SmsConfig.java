package com.javabaas.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 短信相关设置
 * Created by Codi on 2017/6/29.
 */
@Configuration
@ConfigurationProperties(prefix = "baas.sms")
public class SmsConfig {

    private static final int DEFAULT_TRY_ERROR_LIMIT = 5;
    private static final String DEFAULT_SMS_CODE_TEMPLATE_ID = "0";
    private static final String DEFAULT_SIGN_NAME = "JavaBaas";
    private static final Integer DEFAULT_SEND_TIME_LIMIT = 60;

    private Integer tryErrorLimit; //错误尝试次数
    private Integer sendIntervalLimit;//发送时间间隔(秒)
    private String smsCodeTemplateId; //短信验证码模版id
    private String signName; //短信签名

    public Integer getTryErrorLimit() {
        if (tryErrorLimit == null) {
            return DEFAULT_TRY_ERROR_LIMIT;
        } else {
            return tryErrorLimit;
        }
    }

    public void setTryErrorLimit(int tryErrorLimit) {
        this.tryErrorLimit = tryErrorLimit;
    }

    public Integer getSendIntervalLimit() {
        if (sendIntervalLimit == null) {
            return DEFAULT_SEND_TIME_LIMIT;
        } else {
            return sendIntervalLimit;
        }
    }

    public void setSendIntervalLimit(Integer sendIntervalLimit) {
        this.sendIntervalLimit = sendIntervalLimit;
    }

    public String getSmsCodeTemplateId() {
        if (StringUtils.isEmpty(smsCodeTemplateId)) {
            return DEFAULT_SMS_CODE_TEMPLATE_ID;
        } else {
            return smsCodeTemplateId;
        }
    }

    public void setSmsCodeTemplateId(String smsCodeTemplateId) {
        this.smsCodeTemplateId = smsCodeTemplateId;
    }

    public String getSignName() {
        if (StringUtils.isEmpty(signName)) {
            return DEFAULT_SIGN_NAME;
        } else {
            return signName;
        }
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }
}
