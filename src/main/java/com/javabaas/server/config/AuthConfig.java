package com.javabaas.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Created by Codi on 15/12/17.
 */
@Configuration
@ConfigurationProperties(prefix = "baas.auth")
public class AuthConfig {

    private static final long DEFAULT_TIMEOUT = 600000;
    private static final String DEFAULT_ADMIN_KEY = "JavaBaas";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private static final boolean DEFAULT_ENABLE = true;

    private String key;
    private String username;
    private String password;
    private Boolean enable;

    private long timeout;

    public String getKey() {
        if (key == null) {
            return DEFAULT_ADMIN_KEY;
        } else {
            return key;
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTimeout() {
        if (timeout == 0) {
            return DEFAULT_TIMEOUT;
        } else {
            return timeout;
        }
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getUsername() {
        if (StringUtils.isEmpty(username)) {
            return DEFAULT_USERNAME;
        } else {
            return username;
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        if (StringUtils.isEmpty(password)) {
            return DEFAULT_PASSWORD;
        } else {
            return password;
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnable() {
        if (enable == null) {
            return DEFAULT_ENABLE;
        } else {
            return enable;
        }
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
