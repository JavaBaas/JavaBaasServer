package com.staryet.baas.admin.service;

import com.staryet.baas.common.entity.SimpleCode;
import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Created by Codi on 16/2/25.
 */
@Service
public class ConsoleService {

    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public String getSessionToken(String username, String password) {
        String passwordEncrypt = encrypt(authConfig.getUsername(), authConfig.getPassword());
        if (username.equals(authConfig.getUsername()) && password.equals(passwordEncrypt)) {
            return getSessionToken(username);
        } else {
            throw new SimpleError(SimpleCode.CONSOLE_USER_ERROR);
        }
    }

    public String getSessionToken(String username) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String sessionToken = ops.get("SessionToken_" + username);
        if (StringUtils.isEmpty(sessionToken)) {
            sessionToken = createSessionToken();
            ops.set("SessionToken_" + username, sessionToken);
        }
        return sessionToken;
    }

    public void removeSessionToken(String username) {
        redisTemplate.delete("SessionToken_" + username);
    }

    private String encrypt(String username, String password) {
        return DigestUtils.md5DigestAsHex((username + "_._" + password).getBytes());
    }

    private String createSessionToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
