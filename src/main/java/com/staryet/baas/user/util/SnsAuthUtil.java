package com.staryet.baas.user.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.common.util.JSONUtil;
import com.staryet.baas.user.entity.BaasAuth;
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

    public boolean verifyAuthData(String platform, BaasAuth auth) {
        switch (platform) {
            case "qq":
                return verifyQq(auth);
            case "weibo":
                return verifyWeibo(auth);
            case "weixin":
                return verifyWeixin(auth);
        }
        return false;
    }

    private boolean verifyWeibo(BaasAuth auth) {
        //请求新浪授权服务器以验证授权是否有效
        try {
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
        //请求微信授权服务器验证授权是否有效
        String resultStr = rest.getForObject("https://api.weixin.qq.com/sns/auth?access_token={access_token}&openid={openid}", String.class, auth.getAccessToken(), auth.getUid());
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

    private void errorLog(String platform, BaasAuth auth, String result, String message) {
        try {
            log.debug("第三方鉴权失败 平台:" + platform + "授权信息:" + jsonUtil.writeValueAsString(auth) + "返回信息:" + result + "原因:" + message);
        } catch (SimpleError ignored) {
        }
    }

}
