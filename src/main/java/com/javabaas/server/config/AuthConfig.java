package com.javabaas.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Codi on 15/12/17.
 */
@Configuration
@ConfigurationProperties(prefix = "baas.auth")
public class AuthConfig {

    private static final long DEFAULT_TIMEOUT = 600000;
    private static final String DEFAULT_ADMIN_KEY = "JavaBaas";

    private String key;

    private long timeout;

    public String getAdminKey() {
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

}
