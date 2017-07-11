package com.javabaas.server.user.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javabaas.server.admin.entity.Account;
import com.javabaas.server.admin.entity.AccountType;
import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.user.entity.BaasAuth;
import com.javabaas.server.user.entity.BaasSnsType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方授权验证
 * Created by Codi on 15/10/14.
 */
@Component
public class SnsAuthUtil {

    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private RestTemplate rest;
    @Autowired
    private JSONUtil jsonUtil;
    @Autowired
    private AppService appService;

    public boolean verifyAuthData(String appId, BaasSnsType snsType, BaasAuth auth) {
        switch (snsType) {
            case QQ:
                return verifyQq(auth);
            case WEIBO:
                return verifyWeibo(auth);
            case WEIXIN:
                return verifyWeixin(auth);
            case WEBAPP:
                return verifyWebApp(appId, auth);
        }
        return false;
    }

    private boolean verifyWeibo(BaasAuth auth) {
        //请求新浪授权服务器以验证授权是否有效
        try {
            if (StringUtils.isEmpty(auth.getUid()) || StringUtils.isEmpty(auth.getAccessToken())) {
                throw new SimpleError(SimpleCode.USER_AUTH_ERROR);
            }
            String resultStr = rest.postForObject("https://api.weibo.com/oauth2/get_token_info?access_token={accessToken}", null, String.class, auth.getAccessToken());
            if (StringUtils.isEmpty(resultStr)) {
                //返回信息为空 鉴权失败
                errorLog("weibo", auth, resultStr, "返回信息为空");
                return false;
            }
            Map<String, String> result = jsonUtil.readValue(resultStr, new TypeReference<HashMap<String, String>>() {
            });
            String uid = result.get("uid");
            if (StringUtils.isEmpty(uid) || !uid.equals(auth.getUid())) {
                //uid不匹配 鉴权失败
                errorLog("weibo", auth, resultStr, "id不匹配");
                return false;
            }
            //全部通过 鉴权成功
            return true;
        } catch (Exception t) {
            log.error("微博鉴权失败!");
            log.error(t, t);
            return false;
        }
    }

    private boolean verifyWeixin(BaasAuth auth) {
        if (StringUtils.isEmpty(auth.getOpenId()) && StringUtils.isEmpty(auth.getUnionId()) || StringUtils.isEmpty(auth.getAccessToken())) {
            throw new SimpleError(SimpleCode.USER_AUTH_ERROR);
        }
        //请求微信授权服务器验证授权是否有效
        String resultStr = rest.getForObject("https://api.weixin.qq.com/sns/auth?access_token={access_token}&openid={openid}", String.class, auth.getAccessToken(), auth.getOpenId());
        if (StringUtils.isEmpty(resultStr)) {
            //返回信息为空 鉴权失败
            errorLog("weixin", auth, resultStr, "返回信息为空");
            return false;
        }
        Map<String, String> result = jsonUtil.readValue(resultStr, new TypeReference<HashMap<String, String>>() {
        });
        String code = result.get("errcode");
        return "0".equals(code);
    }

    private boolean verifyQq(BaasAuth auth) {
        if (StringUtils.isEmpty(auth.getOpenId()) && StringUtils.isEmpty(auth.getUnionId()) || StringUtils.isEmpty(auth.getAccessToken())) {
            throw new SimpleError(SimpleCode.USER_AUTH_ERROR);
        }
        //请求qq授权服务器验证授权是否有效
        try {
            String resultStr = rest.getForObject("https://graph.qq.com/oauth2.0/me?access_token={access_token}", String.class, auth.getAccessToken());
            if (StringUtils.isEmpty(resultStr)) {
                //返回信息为空 鉴权失败
                errorLog("qq", auth, resultStr, "返回信息为空");
                return false;
            }
            resultStr = resultStr.substring(resultStr.indexOf("{"), resultStr.indexOf("}") + 1);
            Map<String, String> result = jsonUtil.readValue(resultStr, new TypeReference<HashMap<String, String>>() {
            });
            String uid = result.get("openid");
            if (StringUtils.isEmpty(uid) || !uid.equals(auth.getUid())) {
                //uid不匹配 鉴权失败
                errorLog("qq", auth, resultStr, "id不匹配");
                return false;
            }
            //鉴权成功
            return true;
        } catch (Exception t) {
            log.error("QQ鉴权失败!");
            log.error(t, t);
            return false;
        }
    }

    private boolean verifyWebApp(String appId, BaasAuth auth) {
        if (StringUtils.isEmpty(auth.getEncryptedData()) || StringUtils.isEmpty(auth.getCode()) || StringUtils.isEmpty(auth.getIV())) {
            throw new SimpleError(SimpleCode.USER_AUTH_ERROR);
        }
        App app = appService.get(appId);
        Account account = app.getAppAccounts().getAccount(AccountType.WEBAPP);
        if (account == null) {
            throw new SimpleError(SimpleCode.APP_WEBAPP_ACCOUNT_ERROR);
        }

        try {
            //小程序唯一标识
            String waAppid = account.getKey();
            //小程序的 app secret
            String waSecret = account.getSecret();
            String grant_type = "authorization_code";

            //////////////// 1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid ////////////////
            //请求参数
            String param = "appid=" + waAppid + "&secret=" + waSecret + "&js_code=" + auth.getCode() + "&grant_type=" + grant_type;
            //发送请求
            String resultStr = rest.getForObject("https://api.weixin.qq.com/sns/jscode2session?" + param, String.class);
            //解析相应内容
            Map<String, String> resultObject = jsonUtil.readValue(resultStr, new TypeReference<HashMap<String, String>>() {
            });

            //获取会话密钥（session_key）
            String session_key = resultObject.get("session_key");

            //////////////// 2、对encryptedData加密数据进行AES解密 ////////////////

            String result = AesCbcUtil.decrypt(auth.getEncryptedData(), session_key, auth.getIV(), "UTF-8");
            if (null != result && result.length() > 0) {
                Map<String, Object> userInfoObject = jsonUtil.readValue(result, new TypeReference<HashMap<String, Object>>() {
                });
                auth.setOpenId((String) userInfoObject.get("openId"));
                String unionId = (String) userInfoObject.get("unionId");
                if (!StringUtils.isEmpty(unionId)) {
                    auth.setUnionId(unionId);
                }
                return true;
            }
        } catch (Exception e) {
            log.error("小程序鉴权失败!");
            log.error(e, e);
        }
        return false;
    }

    private void errorLog(String platform, BaasAuth auth, String result, String message) {
        try {
            log.debug("第三方鉴权失败 平台:" + platform + "授权信息:" + jsonUtil.writeValueAsString(auth) + "返回信息:" + result + "原因:" + message);
        } catch (SimpleError ignored) {
        }
    }

}
