package com.javabaas.server.common.interceptor;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * API权限拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AppService appService;
    @Autowired
    private AuthConfig authConfig;

    /**
     * 验证授权信息
     * sign为 md5(key:timestamp)
     *
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o)
            throws Exception {
        String appId = httpServletRequest.getHeader("JB-AppId");
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
                //时间戳失效时间为10分钟
                throw new SimpleError(SimpleCode.AUTH_TIME_OUT);
            }
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

    public String encrypt(String key, String timeStamp) {
        return DigestUtils.md5DigestAsHex((key + ":" + timeStamp).getBytes());
    }

}
