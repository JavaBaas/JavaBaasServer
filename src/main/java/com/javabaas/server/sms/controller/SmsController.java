package com.javabaas.server.sms.controller;

import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.sms.entity.SmsSendResult;
import com.javabaas.server.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 短信发送
 * Created by Codi on 15/11/2.
 */
@RestController
@RequestMapping(value = "/api/master/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;
    @Autowired
    private JSONUtil jsonUtil;

    /**
     * 推送消息
     *
     * @return 结果
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult send(@RequestHeader(value = "JB-AppId") String appId,
                             @RequestHeader(value = "JB-Plat") String plat,
                             @RequestParam String phoneNumber,
                             @RequestParam String templateId,
                             @RequestParam(required = false) String params) {
        BaasObject paramsObject = StringUtils.isEmpty(params) ? null : jsonUtil.readValue(params, BaasObject.class);
        SmsSendResult sendResult = smsService.sendSms(appId, plat, phoneNumber, templateId, paramsObject);
        return SimpleResult.success().putData("sendResult", sendResult);
    }

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 手机号码
     * @param ttl         失效时间
     */
    @RequestMapping(value = "/smsCode", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult smsCode(@RequestHeader(value = "JB-AppId") String appId,
                                @RequestHeader(value = "JB-Plat") String plat,
                                @RequestParam String phoneNumber,
                                @RequestParam long ttl) {
        SmsSendResult sendResult = smsService.sendSmsCode(appId, plat, phoneNumber, ttl);
        return SimpleResult.success().putData("sendResult", sendResult);
    }

    /**
     * 验证短信验证码
     *
     * @param phoneNumber 手机号码
     * @param code        验证码
     */
    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult smsCode(@RequestHeader(value = "JB-AppId") String appId,
                                @RequestHeader(value = "JB-Plat") String plat,
                                @RequestParam String phoneNumber,
                                @RequestParam String code) {
        boolean result = smsService.verifySmsCode(appId, phoneNumber, code);
        return SimpleResult.success().putData("verifyResult", result);
    }

}
