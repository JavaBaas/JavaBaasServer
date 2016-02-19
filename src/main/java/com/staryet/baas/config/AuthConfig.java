package com.staryet.baas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Codi on 15/12/17.
 */
@Configuration
@ConfigurationProperties(prefix = "baas.auth")
public class AuthConfig {

    public static final long DEFAULT_TIMEOUT = 600000;

    private String key;
    private long timeout;

    public String getKey() {
        if (key == null) {
            return "";
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
}
