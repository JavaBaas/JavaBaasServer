package com.javabaas.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Codi on 15/12/17.
 */
@Configuration
@ConfigurationProperties(prefix = "baas")
public class BaasConfig {

    public static final String DEFAULT_HOST = "http://127.0.0.1:8080/";

    private String host;
    @Autowired
    private AuthConfig authConfig;

    public String getHost() {
        if (host == null) {
            return DEFAULT_HOST;
        } else {
            return host;
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public AuthConfig getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(AuthConfig authConfig) {
        this.authConfig = authConfig;
    }
}
