package com.javabaas.server.config.controller;

import com.javabaas.server.admin.entity.Account;
import com.javabaas.server.common.entity.SimpleResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 应用配置接口
 * Created by Codi on 2017/7/6.
 */
@RestController
@RequestMapping(value = "/api/master/config")
public class AppConfigController {

    /**
     * 设置推送账号信息
     *
     * @param appId   应用id
     * @param account 推送账号信息
     * @return 结果
     */
    @RequestMapping(value = "/setAccount/{type}", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult setAccount(@RequestHeader(value = "JB-AppId") String appId,
                                   @PathVariable int type,
                                   @Valid @RequestBody Account account) {
        return SimpleResult.success();
    }
}
