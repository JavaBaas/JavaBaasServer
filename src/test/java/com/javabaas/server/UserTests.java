package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Field;
import com.javabaas.server.admin.entity.FieldType;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.sms.handler.impl.MockSmsHandler;
import com.javabaas.server.user.entity.BaasAuth;
import com.javabaas.server.user.entity.BaasPhoneRegister;
import com.javabaas.server.user.entity.BaasSnsType;
import com.javabaas.server.user.entity.BaasUser;
import com.javabaas.server.user.service.UserService;
import com.javabaas.server.util.MockClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 测试用户系统
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class UserTests {

    @Autowired
    private UserService userService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private AppService appService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private AppConfigService appConfigService;
    @Autowired
    private JSONUtil jsonUtil;
    @Autowired
    private MockSmsHandler smsHandler;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockClient mockClient;
    private App app;

    @Before
    public void before() {
        mockClient = new MockClient(webApplicationContext);

        appService.deleteByAppName("UserTestApp");
        app = new App();
        app.setName("UserTestApp");
        appService.insert(app);

        Field nickName = new Field(FieldType.STRING, "nickName");
        fieldService.insert(app.getId(), "_User", nickName);

        BaasUser user = new BaasUser();
        user.setUsername("u1");
        user.put("nickName", "u1");
        user.setPassword("bbbbbb");
        user.setPhone("13813813838");
        userService.register(app.getId(), "cloud", user);

        user = new BaasUser();
        user.setUsername("u2");
        user.put("nickName", "u2");
        user.setPassword("bbbbbb");
        userService.register(app.getId(), "cloud", user);

        //配置短信发送器为测试发送器
        appConfigService.setConfig(app.getId(), AppConfigEnum.SMS_HANDLER, "mockSmsHandler");

    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testRegister() {
        //测试不合法的用户名注册
        BaasUser user = new BaasUser();
        user.setUsername("#aa");
        user.setPassword("aaaaaa");
        try {
            userService.register(app.getId(), "cloud", user);
            Assert.fail("非法的用户名注册成功");
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.USER_INVALID_USERNAME.getCode()));
        }

        //测试正常的用户注册
        user = new BaasUser();
        user.setUsername("testRegister");
        user.setPassword("aaaaaa");
        userService.register(app.getId(), "cloud", user);

        //登录测试
        user = userService.login(app.getId(), "admin", "testRegister", "aaaaaa");
        Assert.assertThat(user.getUsername(), equalTo("testRegister"));
        Assert.assertThat(user.getPassword(), nullValue());
        Assert.assertThat(user.getSessionToken(), anything());
        //缓存
        String sessionToken = user.getSessionToken();
        BaasUser cache = userService.getUserBySessionToken(app.getId(), "admin", sessionToken);
        Assert.assertThat(cache.getUsername(), equalTo("testRegister"));
    }

    @Test
    public void testGet() {
        //测试普通权限无法读取保密字段
        List<BaasObject> users = objectService.list(app.getId(), "admin", UserService.USER_CLASS_NAME, null, null, null, 100, 0, null,
                false);
        Assert.assertThat(users.size(), equalTo(2));

        BaasUser user1 = new BaasUser(users.get(0));
        Assert.assertThat(user1.getSessionToken(), nullValue());
        Assert.assertThat(user1.getAuth(), nullValue());
        Assert.assertThat(user1.getPassword(), nullValue());
        Assert.assertThat(user1.getUsername(), equalTo("u2"));
    }

    @Test
    public void testLogin() {
        //登录测试
        BaasUser user = userService.login(app.getId(), "admin", "u1", "bbbbbb");
        Assert.assertThat(user.getUsername(), equalTo("u1"));
        Assert.assertThat(user.get("nickName"), equalTo("u1"));
        Assert.assertThat(user.getPassword(), nullValue());
        Assert.assertThat(user.getSessionToken(), anything());

        //错误密码
        try {
            userService.login(app.getId(), "admin", "u1", "bbb");
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.USER_WRONG_PASSWORD.getCode()));
        }
    }

    @Test
    public void testUpdate() {
        BaasUser user = userService.login(app.getId(), "admin", "u1", "bbbbbb");
        String id = user.getId();

        //测试修改信息
        BaasUser newUser = new BaasUser();
        newUser.put("nickName", "u1new");
        try {
            //非本人禁止修改
            userService.update(app.getId(), "admin", id, newUser, null, false);
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.USER_NOT_MATCH.getCode()));
        }

        userService.update(app.getId(), "admin", id, newUser, user, false);
        user = userService.login(app.getId(), "admin", "u1", "bbbbbb");
        String oldSessionToken = user.getSessionToken();
        Assert.assertThat(user.get("nickName"), equalTo("u1new"));

        //测试修改密码
        try {
            userService.updatePassword(app.getId(), "admin", id, "cccccc", "cccccc", user);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.USER_WRONG_PASSWORD.getCode()));
        }
        userService.updatePassword(app.getId(), "admin", id, "bbbbbb", "cccccc", user);
        user = userService.login(app.getId(), "admin", "u1", "cccccc");
        Assert.assertThat(user.getUsername(), equalTo("u1"));

        //测试SessionToken是否被重置
        Assert.assertThat(user.getSessionToken(), not(equalTo(oldSessionToken)));

    }

    @Test
    public void testSns() {
        BaasAuth auth = new BaasAuth();
        auth.put("accessToken", "2.00btHZGCqfLC4Dca97ee7070S4FQEC");
        auth.put("uid", "1929295515");
        //绑定微博
        BaasUser user1 = userService.get(app.getId(), "admin", "u1", null, true);
        userService.bindingSns(app.getId(), "admin", user1.getId(), BaasSnsType.WEIBO, auth, user1, false);
        //验证是否绑定成功
        user1 = userService.get(app.getId(), "admin", "u1", null, true);
        BaasObject user1Auth = user1.getAuth();
        BaasAuth weiboAuth = new BaasAuth(user1Auth.getBaasObject("weibo"));
        Assert.assertThat(weiboAuth.getUid(), equalTo("1929295515"));

        //使用第三方授权信息登录
        user1 = userService.registerWithSns(app.getId(), "admin", BaasSnsType.WEIBO, weiboAuth, null);
        Assert.assertThat(user1.getUsername(), equalTo("u1"));
        //再次登录测试
        user1 = userService.registerWithSns(app.getId(), "admin", BaasSnsType.WEIBO, weiboAuth, null);
        Assert.assertThat(user1.getUsername(), equalTo("u1"));

        //验证禁止重复绑定
        try {
            userService.bindingSns(app.getId(), "admin", user1.getId(), BaasSnsType.WEIBO, auth, user1, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.USER_AUTH_EXIST.getCode()));
        }

        //验证解除绑定成功
        userService.releaseSns(app.getId(), "admin", user1.getId(), "weibo", user1, false);
        user1 = userService.get(app.getId(), "admin", "u1", null, true);
        user1Auth = user1.getAuth();
        Assert.assertThat(user1Auth.get("weibo"), nullValue());
    }

    @Test
    public void testResetSessionToken() {
        BaasUser user1 = userService.get(app.getId(), "admin", "u1", null, true);
        String sessionToken = user1.getSessionToken();
        Assert.assertThat(sessionToken, notNullValue());
        //修改sessionToken
        userService.resetSessionToken(app.getId(), "admin", user1.getId());
        user1 = userService.get(app.getId(), "admin", "u1", null, true);
        String sessionTokenNew = user1.getSessionToken();
        Assert.assertThat(sessionTokenNew, notNullValue());
        Assert.assertThat(sessionTokenNew, not(sessionToken));
    }

    /**
     * 测试手机号注册
     */
    @Test
    public void testRegisterByPhone() throws Exception {
        //获取短信验证码
        mockClient.user(app, HttpMethod.GET, "/api/user/getSmsCode/13800138000", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SimpleCode.SUCCESS.getCode())));
        String code = smsHandler.getSms("13800138000");

        //使用验证码注册
        BaasPhoneRegister register = new BaasPhoneRegister();
        register.setPhone("13800138000");
        register.setCode(code);
        mockClient.user(app, HttpMethod.POST, "/api/user/loginWithPhone", jsonUtil.writeValueAsString(register))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone", is("13800138000")))
                .andExpect(jsonPath("$.username", not(empty())));
    }

    /**
     * 测试手机号登录
     */
    @Test
    public void testLoginByPhone() throws Exception {
        //获取短信验证码
        mockClient.user(app, HttpMethod.GET, "/api/user/getSmsCode/13813813838", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.code", is(SimpleCode.SUCCESS.getCode())));
        String code = smsHandler.getSms("13813813838");

        //使用验证码注册
        BaasPhoneRegister register = new BaasPhoneRegister();
        register.setPhone("13813813838");
        register.setCode(code);
        mockClient.user(app, HttpMethod.POST, "/api/user/loginWithPhone", jsonUtil.writeValueAsString(register))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone", is("13813813838")))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.sessionToken", not(empty())))
                .andExpect(jsonPath("$.username", not(empty())));
    }

}
