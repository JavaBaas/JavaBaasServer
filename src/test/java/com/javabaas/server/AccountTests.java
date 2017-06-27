package com.javabaas.server;

import com.javabaas.server.admin.entity.Account;
import com.javabaas.server.admin.entity.AccountType;
import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by test on 2017/6/19.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class,webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccountTests {
    @Autowired
    private AppService appService;

    private String appId;

    @Before
    public void before() {
        appService.deleteByAppName("AccountTestApp");
        App app = new App();
        app.setName("AccountTestApp");
        appService.insert(app);
        appId = app.getId();
    }

    @After
    public void after() {
        appService.delete(appId);
    }

    @Test
    public void testAccount() {
        Account pushAccount = new Account();
        pushAccount.setKey("pushKey");
        pushAccount.setSecret("pushSecret");
        appService.setAccount(appId, AccountType.PUSH, pushAccount);
        App app = appService.get(appId);
        Assert.assertThat(app.getAppAccounts().getAccount(AccountType.PUSH).getKey(), equalTo("pushKey"));
        Assert.assertThat(app.getAppAccounts().getAccount(AccountType.PUSH).getSecret(), equalTo("pushSecret"));
        Account webappAccount = new Account();
        webappAccount.setKey("webappKey");
        webappAccount.setSecret("webappSecret");
        appService.setAccount(appId, AccountType.WEBAPP, webappAccount);
        app = appService.get(appId);
        Assert.assertThat(app.getAppAccounts().size(), equalTo(2));
    }

}
