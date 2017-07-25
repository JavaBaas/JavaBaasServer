package com.javabaas.server.common.interceptor;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.sign.ReplyChecker;
import com.javabaas.server.common.sign.SignUtil;
import com.javabaas.server.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
    @Autowired
    private ReplyChecker replyChecker;

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
            String nonce = httpServletRequest.getHeader("JB-Nonce");
            if (StringUtils.isEmpty(timestampStr) || StringUtils.isEmpty(appId) || StringUtils.isEmpty(nonce) ||
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
                replyChecker.checkSignReplay(appId, sign, masterSign);
                //判断鉴权类型
                if (!StringUtils.isEmpty(masterSign)) {
                    //使用管理授权
                    App app = appService.get(appId);
                    String masterKey = app.getMasterKey();
                    //验证
                    if (!masterSign.equals(SignUtil.encrypt(masterKey, timestampStr, nonce))) {
                        //验证失败
                        throw new SimpleError(SimpleCode.AUTH_ERROR);
                    }
                } else if (!StringUtils.isEmpty(sign)) {
                    //使用普通授权
                    App app = appService.get(appId);
                    String key = app.getKey();
                    //验证
                    if (!sign.equals(SignUtil.encrypt(key, timestampStr, nonce))) {
                        //验证失败
                        throw new SimpleError(SimpleCode.AUTH_ERROR);
                    }
                }
                //验证成功后记录sign用于防重放攻击
                replyChecker.recordSign(appId, sign);
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

}
