package com.javabaas.server.common.sign;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 授权信息检查器
 * Created by Codi on 2017/10/17.
 */
@Component
public class AuthChecker {

    @Autowired
    private AppService appService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private ReplyChecker replyChecker;

    /**
     * 超级权限检查
     */
    public void adminCheck(HttpServletRequest httpServletRequest) {
        String adminKey = httpServletRequest.getHeader("JB-AdminKey");
        String adminSign = httpServletRequest.getHeader("JB-AdminSign");
        String timestampStr = httpServletRequest.getHeader("JB-Timestamp");
        String nonce = httpServletRequest.getHeader("JB-Nonce");
        if (!StringUtils.isEmpty(adminKey)) {
            //使用key鉴权模式
            if (!authConfig.getAdminKey().equals(adminKey)) {
                //验证失败
                throw new SimpleError(SimpleCode.AUTH_ERROR);
            }
        } else {
            //使用签名鉴权模式
            if (StringUtils.isEmpty(adminSign) || StringUtils.isEmpty(timestampStr) || StringUtils.isEmpty(nonce)) {
                //缺少超级权限
                throw new SimpleError(SimpleCode.AUTH_NEED_ADMIN_AUTH);
            }
            //验证时间戳
            Long timestamp = Long.valueOf(timestampStr);
            checkTimestamp(timestamp);
            //验证sign
            checkSign(adminSign, authConfig.getAdminKey(), timestampStr, nonce);
        }
    }

    /**
     * 管理权限检查
     * 只检查请求头是否带有JB-MasterSign
     * 校验工作由权限检查完成
     */
    public void masterCheck(HttpServletRequest httpServletRequest) {
        String masterSign = httpServletRequest.getHeader("JB-MasterSign");
        String masterKey = httpServletRequest.getHeader("JB-MasterKey");
        if (StringUtils.isEmpty(masterSign) && StringUtils.isEmpty(masterKey)) {
            //缺少管理员权限
            throw new SimpleError(SimpleCode.AUTH_NEED_MASTER_AUTH);
        }
    }

    /**
     * 权限检查
     */
    public void authCheck(HttpServletRequest httpServletRequest) {
        String key = httpServletRequest.getHeader("JB-Key");
        String masterKey = httpServletRequest.getHeader("JB-MasterKey");
        String appId = httpServletRequest.getHeader("JB-AppId");
        String masterSign = httpServletRequest.getHeader("JB-MasterSign");
        String sign = httpServletRequest.getHeader("JB-Sign");
        String timestampStr = httpServletRequest.getHeader("JB-Timestamp");
        String nonce = httpServletRequest.getHeader("JB-Nonce");
        if (StringUtils.isEmpty(appId)) {
            //缺少appId
            throw new SimpleError(SimpleCode.AUTH_APP_ID_LESS);
        }
        //获取当前应用信息
        App app = appService.get(appId);
        if (app == null) {
            //应用不存在
            throw new SimpleError(SimpleCode.APP_NOT_FOUND);
        }
        //鉴权 当masterKey或者masterSign存在时 优先鉴权master权限
        if (!StringUtils.isEmpty(masterKey)) {
            //使用masterKey鉴权
            if (!masterKey.equals(app.getMasterKey())) {
                //验证失败
                throw new SimpleError(SimpleCode.AUTH_ERROR);
            }
        } else if (!StringUtils.isEmpty(masterSign)) {
            //使用masterSign鉴权
            checkAuth(appId, app.getMasterKey(), masterSign, timestampStr, nonce);
        } else if (!StringUtils.isEmpty(key)) {
            //使用key鉴权
            if (!key.equals(app.getKey())) {
                //验证失败
                throw new SimpleError(SimpleCode.AUTH_ERROR);
            }
        } else if (!StringUtils.isEmpty(sign)) {
            //使用sign鉴权
            checkAuth(appId, app.getKey(), sign, timestampStr, nonce);
        } else {
            //未进入任何授权验证逻辑 授权信息不足
            throw new SimpleError(SimpleCode.AUTH_LESS);
        }

    }

    private void checkTimestamp(long timestamp) {
        long timestampNow = new Date().getTime();
        if (Math.abs(timestamp - timestampNow) > authConfig.getTimeout()) {
            //时间戳失效时间为10分钟
            throw new SimpleError(SimpleCode.AUTH_TIME_OUT);
        }
    }

    private void checkAuth(String appId, String key, String sign, String timestamp, String nonce) {
        if (StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(nonce)) {
            //授权信息不足
            throw new SimpleError(SimpleCode.AUTH_LESS);
        }
        //验证时间戳
        Long time = Long.valueOf(timestamp);
        checkTimestamp(time);
        //防重放攻击
        replyChecker.checkSignReplay(appId, sign);
        //验证sign
        checkSign(sign, key, timestamp, nonce);
        //验证成功后记录sign用于防重放攻击
        replyChecker.recordSign(appId, sign);
    }

    private void checkSign(String sign, String key, String timestamp, String nonce) {
        //使用超级授权
        if (!sign.equals(SignUtil.encrypt(key, timestamp, nonce))) {
            //验证失败
            throw new SimpleError(SimpleCode.AUTH_ERROR);
        }
    }

}
