package com.javabaas.server.admin.controller;

import com.javabaas.server.admin.entity.Account;
import com.javabaas.server.admin.entity.AccountType;
import com.javabaas.server.admin.service.AccountService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by test on 2017/6/19.
 */
@RestController
@RequestMapping(value = "/api/master/account")
public class AccountController {
    @Autowired
    private AccountService accountService;

    /**
     * 设置推送账号信息
     *
     * @param appId   应用id
     * @param account 推送账号信息
     * @return 结果
     * @throws SimpleError
     */
    @RequestMapping(value = "/setAccount", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult setAccount(@RequestHeader(value = "JB-AppId") String appId,
                                       @PathVariable int type,
                                       @Valid @RequestBody Account account) {
        AccountType accountType = AccountType.getType(type);
        if (accountType == null) {
            throw new SimpleError(SimpleCode.APP_ACCOUNT_ERROR);
        }
        accountService.setAccount(appId,accountType, account);
        return SimpleResult.success();
    }
}
