package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.config.AuthConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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

    private MockMvc mockMvc;
    private App app;

    @Autowired
    private AppService appService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
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
        mockMvc.perform(defaultRequest("/api/admin/app"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_NEED_ADMIN_SIGN.getCode())));
    }

    @Test
    public void testNoMasterAuthUrl() throws Exception {
        mockMvc.perform(userRequest("/api/master/clazz"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_NEED_MASTER_SIGN.getCode())));
        mockMvc.perform(userRequest("/api/master/clazz/Book/field"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_NEED_MASTER_SIGN.getCode())));
    }

    @Test
    public void testNoUserAuthUrl() throws Exception {
        mockMvc.perform(defaultRequest("/api/object/Book"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_LESS.getCode())));
    }

    @Test
    public void testAdminAuth() throws Exception {
        mockMvc.perform(adminRequest("/api/admin/app"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testMasterAuth() throws Exception {
        mockMvc.perform(masterRequest("/api/master/clazz/Book"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testUserAuth() throws Exception {
        mockMvc.perform(userRequest("/api/object/Book"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    /**
     * 测试拒绝重放攻击
     */
    public void testReplayAttack() throws Exception {
        long timestamp = new Date().getTime();
        String timestampStr = String.valueOf(timestamp);
        String sign = getSign(app.getKey(), timestampStr);
        String url = "/api/object/Book";
        mockMvc.perform(get(url).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-Sign", sign)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        //重放攻击请求被拒绝
        mockMvc.perform(get(url).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-Sign", sign)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code", is(SimpleCode.AUTH_REPLAY_ATTACK.getCode())));
    }

    private MockHttpServletRequestBuilder defaultRequest(String url) throws Exception {
        return get(url).header("JB-Plat", "cloud").contentType(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON_UTF8);
    }

    /**
     * 管理权限请求
     *
     * @param url 地址
     */
    private MockHttpServletRequestBuilder masterRequest(String url) throws Exception {
        long timestamp = new Date().getTime();
        String timestampStr = String.valueOf(timestamp);
        return get(url).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-MasterSign", getSign(app.getMasterKey(), timestampStr))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8);
    }

    /**
     * 超级权限请求
     *
     * @param url 地址
     */
    private MockHttpServletRequestBuilder adminRequest(String url) throws Exception {
        long timestamp = new Date().getTime();
        String timestampStr = String.valueOf(timestamp);
        return get(url).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-AdminSign", getSign(authConfig.getKey(), timestampStr))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8);
    }

    /**
     * 用户级别请求
     *
     * @param url 地址
     */
    private MockHttpServletRequestBuilder userRequest(String url) throws Exception {
        long timestamp = new Date().getTime();
        String timestampStr = String.valueOf(timestamp);
        return get(url).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-Sign", getSign(app.getKey(), timestampStr))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8);
    }

    private String getSign(String key, String timestamp) {
        return DigestUtils.md5DigestAsHex((key + ":" + timestamp).getBytes());
    }

}
