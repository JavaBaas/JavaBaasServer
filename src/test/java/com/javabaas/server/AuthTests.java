package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.config.AuthConfig;
import com.javabaas.server.util.MockClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by Staryet on 15/9/22.
 * 权限相关测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class AuthTests {

    @Autowired
    private AppService appService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockClient client;
    private App app;

    @Before
    public void setUp() throws Exception {
        client = new MockClient(webApplicationContext);
        app = new App();
        app.setName("AuthTestApp");
        appService.insert(app);
        //创建用于测试的类
        Clazz book = new Clazz("Book");
        clazzService.insert(app.getId(), book);
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testNoAdminAuthUrl() throws Exception {
        client.normal(HttpMethod.GET, "/api/admin/app")
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_NEED_ADMIN_SIGN.getCode())));
    }

    @Test
    public void testNoMasterAuthUrl() throws Exception {
        client.user(app, HttpMethod.GET, "/api/master/clazz", null)
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_NEED_MASTER_SIGN.getCode())));
        client.user(app, HttpMethod.GET, "/api/master/clazz/Book/field", null)
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_NEED_MASTER_SIGN.getCode())));
    }

    @Test
    public void testNoUserAuthUrl() throws Exception {
        client.normal(HttpMethod.GET, "/api/object/Book")
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_APP_ID_LESS.getCode())));
    }

    @Test
    public void testAdminAuth() throws Exception {
        client.admin(authConfig.getKey(), HttpMethod.GET, "/api/admin/app", null)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testMasterAuth() throws Exception {
        client.master(app, HttpMethod.GET, "/api/master/clazz/Book", null)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testUserAuth() throws Exception {
        client.user(app, HttpMethod.GET, "/api/object/Book", null)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }


    /**
     * 测试拒绝重放攻击
     */
    @Test
    public void testReplayAttack() throws Exception {
        long timestamp = new Date().getTime();
        String timestampStr = String.valueOf(timestamp);
        String sign = getSign(app.getKey(), timestampStr);
        String url = "/api/object/Book";
        client.getMockMvc().perform(get(url).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-Sign", sign)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        //重放攻击请求被拒绝
        client.getMockMvc().perform(get(url).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-Sign", sign)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_REPLAY_ATTACK.getCode())));
    }

    private String getSign(String key, String timestamp) {
        return DigestUtils.md5DigestAsHex((key + ":" + timestamp).getBytes());
    }

}
