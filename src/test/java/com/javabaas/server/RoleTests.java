package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.object.entity.BaasAcl;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.role.entity.BaasRole;
import com.javabaas.server.role.service.RoleService;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by zangyilin on 2018/4/19.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class RoleTests {
    @Autowired
    private RoleService roleService;
    @Autowired
    private AppService appService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private JSONUtil jsonUtil;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockClient mockClient;
    private App app;
    private BaasUser user;
    private static final String appName = "RoleTestApp";

    @Before
    public void before() {
        mockClient = new MockClient(webApplicationContext);

        appService.deleteByAppName(appName);
        app = new App();
        app.setName(appName);
        appService.insert(app);

        // 创建用户A
        user = new BaasUser();
        user.setUsername("A");
        user.setPassword("aaaaaa");
        user = userService.register(app.getId(), "cloud", user);

    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testCreate() {
        // 测试不含角色名的创建
        BaasRole role = new BaasRole();
        BaasAcl acl = new BaasAcl();
        acl.setPublicReadAccess(true);
        role.setAcl(acl);
        try {
            roleService.insert(app.getId(), "cloud", role, null, false);
            Assert.fail("不含角色名的角色创建成功");
        } catch (SimpleError e) {
            Assert.assertEquals(e.getCode(), SimpleCode.ROLE_EMPTY_NAME.getCode());
        }

        // 测试不含acl的创建
        role = new BaasRole();
        role.setName(String.valueOf(new Date().getTime()));
        try {
            roleService.insert(app.getId(), "cloud", role, null, false);
            Assert.fail("不含acl的角色创建成功");
        } catch (SimpleError e) {
            Assert.assertEquals(e.getCode(), SimpleCode.ROLE_EMPTY_ACL.getCode());
        }

        // 测试角色名不合法的创建
        role = new BaasRole();
        role.setName("总经理");
        acl.setPublicReadAccess(true);
        role.setAcl(acl);
        try {
            roleService.insert(app.getId(), "cloud", role, null, false);
            Assert.fail("非法角色名的角色创建成功");
        } catch (SimpleError e) {
            Assert.assertEquals(e.getCode(), SimpleCode.ROLE_INVALID_NAME.getCode());
        }

        // 测试正常的创建
        role.setName("role1");
        acl.setPublicReadAccess(true);
        role.setAcl(acl);
        roleService.insert(app.getId(), "cloud", role, null, false);

        // 测试角色名已经存在的创建
        role.setName("role1");
        acl.setPublicReadAccess(true);
        acl.setPublicWriteAccess(true);
        role.setAcl(acl);
        try {
            roleService.insert(app.getId(), "cloud", role, null, false);
            Assert.fail("重复角色名的角色创建成功");
        } catch (SimpleError e) {
            Assert.assertEquals(e.getCode(), SimpleCode.ROLE_ALREADY_EXIST.getCode());
        }
    }

    @Test
    public void testUpdate() {
        // 添加一条角色信息
        BaasRole role = new BaasRole();
        role.setName("role1");
        BaasAcl acl = new BaasAcl();
        acl.setPublicReadAccess(true);
        acl.setWriteAccess(user, true);
        role.setAcl(acl);
        String id = roleService.insert(app.getId(), "cloud", role, null, false).getId();

        BaasObject object = objectService.get(app.getId(), "cloud", RoleService.ROLE_CLASS_NAME, id, null, null, null, false);
        role = new BaasRole(object);
        // 检查是否保存成功
        Assert.assertThat(role.getName(), equalTo("role1"));

        // 检查更新角色名称
        role = new BaasRole();
        role.setName("role2");
        roleService.update(app.getId(), "cloud", id, role, user, false);
        object = objectService.get(app.getId(), "cloud", RoleService.ROLE_CLASS_NAME, id, null, null, null, false);
        role = new BaasRole(object);
        Assert.assertEquals(role.getName(), "role1");

    }

















}
