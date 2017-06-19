package com.javabaas.server.admin.entity;

import java.util.LinkedHashMap;

/**
 * Created by test on 2017/6/15.
 */
public class AppAccounts extends LinkedHashMap<String, Account> {
    public AppAccounts() {
        super();
    }

    public Account getAccount(AccountType accountType) {
        return get(accountType.getValue());
    }

    public void setAccount(AccountType accountType, Account account) {
        put(accountType.getValue(), account);
    }
}
