package com.javabaas.server.common.sign;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 防重放攻击检查器
 * Created by Codi on 2017/7/25.
 */
@Component
public class ReplyChecker {

    private static final String SIGN_NAME = "_Sign";

    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 验证签名是否已经存在
     *
     * @param appId      应用id
     * @param sign       签名
     * @param masterSign 管理签名
     */
    public void checkSignReplay(String appId, String sign, String masterSign) {
        //防重放攻击
        String signNow = StringUtils.isEmpty(masterSign) ? sign : masterSign;
        if (redisTemplate.hasKey(getKey(appId, signNow))) {
            //拒绝重放攻击
            throw new SimpleError(SimpleCode.AUTH_REPLAY_ATTACK);
        }
    }

    /**
     * 记录签名
     *
     * @param appId 应用id
     * @param sign  签名
     */
    public void recordSign(String appId, String sign) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(getKey(appId, sign), "1", authConfig.getTimeout(), TimeUnit.SECONDS);
    }

    private String getKey(String appId, String sign) {
        return "App_" + appId + SIGN_NAME + "_" + sign;
    }
}
