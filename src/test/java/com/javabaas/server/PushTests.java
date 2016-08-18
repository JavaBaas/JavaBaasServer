package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.push.service.PushService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 测试推送
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest("server.port:9000")
public class PushTests {

    @Autowired
    private AppService appService;
    @Autowired
    private PushService pushService;

    private App app;

    @Before
    public void before() {
        app = new App();
        app.setName("PushTestApp");
        appService.insert(app);
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testPush() {

    }

}
