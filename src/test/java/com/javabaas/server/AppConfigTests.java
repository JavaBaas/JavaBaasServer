package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;

/**
 * 应用配置测试
 * Created by Codi on 2017/7/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Main.class)
public class AppConfigTests {

    @Autowired
    private AppService appService;
    @Autowired
    private AppConfigService appConfigService;
    private String appId;

    @Before
    public void before() {
        appService.deleteByAppName("AppConfigTestApp");
        App app = new App();
        app.setName("AppConfigTestApp");
        appService.insert(app);
        appId = app.getId();
    }

    @After
    public void after() {
        appService.delete(appId);
    }

    @Test
    public void testSetConfig() {
        appConfigService.setConfig(appId, AppConfigEnum.SMS_HANDLER, "aliyun");
        String smsHandler = appConfigService.getString(appId, AppConfigEnum.SMS_HANDLER);
        Assert.assertThat(smsHandler, equalTo("aliyun"));
    }

}
