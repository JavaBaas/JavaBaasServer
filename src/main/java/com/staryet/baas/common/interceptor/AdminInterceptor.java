package com.staryet.baas.common.interceptor;

import com.staryet.baas.common.entity.SimpleCode;
import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.config.AuthConfig;
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
 * 管理员权限拦截器
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthConfig authConfig;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String adminSign = httpServletRequest.getHeader("JB-AdminSign");
        String timestampStr = httpServletRequest.getHeader("JB-Timestamp");
        if (StringUtils.isEmpty(adminSign)) {
            //缺少超级权限
            throw new SimpleError(SimpleCode.AUTH_NEED_ADMIN_SIGN);
        }
        Long timestamp = Long.valueOf(timestampStr);
        //验证时间戳
        long timestampNow = new Date().getTime();
        if (Math.abs(timestamp - timestampNow) > authConfig.getTimeout()) {
            //时间戳失效时间为10分钟
            throw new SimpleError(SimpleCode.AUTH_TIME_OUT);
        }
        //验证sign
        if (!StringUtils.isEmpty(adminSign)) {
            //使用超级授权
            if (!adminSign.equals(encrypt(authConfig.getKey(), timestampStr))) {
                //验证失败
                throw new SimpleError(SimpleCode.AUTH_ERROR);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    public String encrypt(String key, String timeStamp) {
        return DigestUtils.md5DigestAsHex((key + ":" + timeStamp).getBytes());
    }

}
