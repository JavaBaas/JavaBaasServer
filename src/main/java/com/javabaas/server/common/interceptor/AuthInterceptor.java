package com.javabaas.server.common.interceptor;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * API权限拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static String SIGN_NAME = "_Sign";
    @Autowired
    private AppService appService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 验证授权信息
     * sign为 md5(key:timestamp)
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o)
            throws Exception {
        String appId = httpServletRequest.getHeader("JB-AppId");
        if (StringUtils.isEmpty(appId)) {
            //缺少appId
            throw new SimpleError(SimpleCode.AUTH_APP_ID_LESS);
        }
        if (authConfig.getEnable()) {
            String masterSign = httpServletRequest.getHeader("JB-MasterSign");
            String sign = httpServletRequest.getHeader("JB-Sign");
            String timestampStr = httpServletRequest.getHeader("JB-Timestamp");
            if (StringUtils.isEmpty(timestampStr) ||
                    StringUtils.isEmpty(appId) ||
                    StringUtils.isEmpty(sign) && StringUtils.isEmpty(masterSign)) {
                //授权信息不足
                throw new SimpleError(SimpleCode.AUTH_LESS);
            } else {
                Long timestamp = Long.valueOf(timestampStr);
                //验证时间戳
                long timestampNow = new Date().getTime();
                if (Math.abs(timestamp - timestampNow) > authConfig.getTimeout()) {
                    //时间戳默认失效时间为10分钟
                    throw new SimpleError(SimpleCode.AUTH_TIME_OUT);
                }
                //防重放攻击
                String signNow = StringUtils.isEmpty(masterSign) ? sign : masterSign;
                if (redisTemplate.hasKey(getKey(appId, signNow))) {
                    //拒绝重放攻击
                    throw new SimpleError(SimpleCode.AUTH_REPLAY_ATTACK);
                }
                //判断鉴权类型
                if (!StringUtils.isEmpty(masterSign)) {
                    //使用管理授权
                    App app = appService.get(appId);
                    String masterKey = app.getMasterKey();
                    //验证
                    if (!masterSign.equals(encrypt(masterKey, timestampStr))) {
                        //验证失败
                        throw new SimpleError(SimpleCode.AUTH_ERROR);
                    }
                } else if (!StringUtils.isEmpty(sign)) {
                    //使用普通授权
                    App app = appService.get(appId);
                    String key = app.getKey();
                    //验证
                    if (!sign.equals(encrypt(key, timestampStr))) {
                        //验证失败
                        throw new SimpleError(SimpleCode.AUTH_ERROR);
                    }
                }
                //验证成功记录sign用于防重放攻击
                ValueOperations<String, String> ops = redisTemplate.opsForValue();
                ops.set(getKey(appId, signNow), "1", authConfig.getTimeout(), TimeUnit.SECONDS);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {

    }

    private String encrypt(String key, String timeStamp) {
        return DigestUtils.md5DigestAsHex((key + ":" + timeStamp).getBytes());
    }

    private String getKey(String appId, String sign) {
        return "App_" + appId + SIGN_NAME + "_" + sign;
    }

}
