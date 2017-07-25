package com.javabaas.server.common.sign;

import org.springframework.util.DigestUtils;

/**
 * 签名工具类
 * Created by Codi on 2017/7/25.
 */
public class SignUtil {

    public static String encrypt(String key, String timeStamp, String nonce) {
        return DigestUtils.md5DigestAsHex((key + ":" + timeStamp + ":" + nonce).getBytes());
    }

}
