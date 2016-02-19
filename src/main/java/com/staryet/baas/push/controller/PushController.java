package com.staryet.baas.push.controller;

import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.common.entity.SimpleResult;
import com.staryet.baas.common.util.JSONUtil;
import com.staryet.baas.object.entity.BaasQuery;
import com.staryet.baas.push.entity.Push;
import com.staryet.baas.push.entity.PushAccount;
import com.staryet.baas.push.service.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 推送
 * Created by Codi on 15/11/2.
 */
@RestController
@RequestMapping(value = "/api/master/push")
public class PushController {

    @Autowired
    private PushService pushService;
    @Autowired
    private JSONUtil jsonUtil;

    /**
     * 推送消息
     *
     * @return 结果
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult push(@RequestHeader(value = "JB-AppId") String appId,
                             @RequestHeader(value = "JB-Plat") String plat,
                             @RequestParam(required = false) String where,
                             @Valid @RequestBody Push push) {
        //处理查询字段
        BaasQuery query = StringUtils.isEmpty(where) ? null : jsonUtil.readValue(where, BaasQuery.class);
        pushService.sendPush(appId, plat, query, push);
        return SimpleResult.success();
    }

    /**
     * 设置推送账号信息
     *
     * @param appId   应用id
     * @param account 推送账号信息
     * @return 结果
     * @throws SimpleError
     */
    @RequestMapping(value = "/setPushAccount", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult setPushAccount(@RequestHeader(value = "JB-AppId") String appId,
                                       @RequestHeader(value = "JB-Plat") String plat,
                                       @Valid @RequestBody PushAccount account) {
        pushService.setPushAccount(appId, account);
        return SimpleResult.success();
    }

}
