package com.javabaas.server.admin.service;

import com.javabaas.server.admin.entity.Account;
import com.javabaas.server.admin.entity.AccountType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by test on 2017/6/19.
 */
@Service
public class AccountService {

    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private AppService appService;



    /**
     * 查询账号信息
     *
     * @param appId 应用id
     * @return 推送账号信息
     */
    public Account getAccount(String appId, AccountType accountType) {
        return appService.get(appId).getAppAccounts().getAccount(accountType);
    }

    /**
     * 设置账号信息
     *
     * @param appId appId
     * @param accountType type
     * @param account account
     */
    public void setAccount(String appId,AccountType accountType, Account account) {
        appService.setAccount(appId, accountType, account);
        log.info("App:" + appId + " " + accountType.getValue() + "账号设置成功");
    }

}
