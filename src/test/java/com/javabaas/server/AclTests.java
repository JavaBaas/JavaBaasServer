package com.javabaas.server;

import com.javabaas.server.admin.entity.*;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.object.entity.BaasAcl;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.role.entity.BaasRole;
import com.javabaas.server.role.service.RoleService;
import com.javabaas.server.user.entity.BaasUser;
import com.javabaas.server.user.service.UserService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Created by Staryet on 15/8/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AclTests {

    @Autowired
    private ClazzService clazzService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private UserService userService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AppService appService;
    private App app;

    @Before
    public void before() {
        appService.deleteByAppName("AclTestApp");
        app = new App();
        app.setName("AclTestApp");
        appService.insert(app);

        //测试用户
        BaasUser user1 = new BaasUser();
        user1.setUsername("user1");
        user1.setPassword("aaaaaa");
        String user1Id = userService.register(app.getId(), "cloud", user1).getId();

        BaasUser user2 = new BaasUser();
        user2.setUsername("user2");
        user2.setPassword("aaaaaa");
        String user2Id = userService.register(app.getId(), "cloud", user2).getId();

        BaasUser user3 = new BaasUser();
        user3.setUsername("user3");
        user3.setPassword("aaaaaa");
        String user3Id = userService.register(app.getId(), "cloud", user3).getId();

        // 测试角色
        // 角色1只有用户1
        BaasRole role1 = new BaasRole();
        BaasAcl roleAcl = new BaasAcl();
        roleAcl.setPublicReadAccess(true);
        role1.setAcl(roleAcl);
        role1.setName("role1");
        List<String> users = new ArrayList<>();
        users.add(user1Id);
        role1.put("users", users);
        role1 = roleService.insert(app.getId(), "cloud", role1, null, true);

        // 角色2包含有用户1和2
        BaasRole role2 = new BaasRole();
        role2.setAcl(roleAcl);
        role2.setName("role2");
        users = new ArrayList<>();
        users.add(user1Id);
        users.add(user2Id);
        role2.put("users", users);
        role2 = roleService.insert(app.getId(), "cloud", role2, null, true);

        // 角色3包含角色1
        BaasRole role3 = new BaasRole();
        role3.setAcl(roleAcl);
        role3.setName("role3");
        List<String> roles = new ArrayList<>();
        roles.add(role1.getId());
        role3.put("roles", roles);
        role3 = roleService.insert(app.getId(), "cloud", role3, null, true);

        // 角色4包含角色3和用户3
        BaasRole role4 = new BaasRole();
        role4.setAcl(roleAcl);
        role4.setName("role4");
        roles = new ArrayList<>();
        roles.add(role3.getId());
        role4.put("roles", roles);
        users = new ArrayList<>();
        users.add(user3Id);
        role4.put("users", users);
        role4 = roleService.insert(app.getId(), "cloud", role4, null, true);

        //创建测试用类Master 用户无任何权限 禁止增删改查
        Clazz clazzMaster = new Clazz();
        clazzMaster.setName("AclMaster");
        ClazzAcl acl = new ClazzAcl();
        acl.setPublicAccess(ClazzAclMethod.FIND, false);
        acl.setPublicAccess(ClazzAclMethod.INSERT, false);
        acl.setPublicAccess(ClazzAclMethod.DELETE, false);
        acl.setPublicAccess(ClazzAclMethod.UPDATE, false);
        clazzMaster.setAcl(acl);
        clazzService.insert(app.getId(), clazzMaster);
        BaasObject t = new BaasObject();
        objectService.insert(app.getId(), "cloud", "AclMaster", t, null, true);

        //创建测试用类1 全体可读 不可写入 不可删除 不可修改
        Clazz clazz1 = new Clazz();
        clazz1.setName("AclTest1");
        acl = new ClazzAcl();
        acl.setPublicAccess(ClazzAclMethod.FIND, true);
        acl.setPublicAccess(ClazzAclMethod.INSERT, false);
        acl.setPublicAccess(ClazzAclMethod.DELETE, false);
        acl.setPublicAccess(ClazzAclMethod.UPDATE, false);
        clazz1.setAcl(acl);
        clazzService.insert(app.getId(), clazz1);
        Field fieldString = new Field(FieldType.STRING, "string");
        fieldService.insert(app.getId(), "AclTest1", fieldString);
        t = new BaasObject();
        t.put("string", "string");
        objectService.insert(app.getId(), "cloud", "AclTest1", t, null, true);

        //创建测试用类2 user1可读 user1可写 user2可删 user2可修改
        Clazz clazz2 = new Clazz();
        clazz2.setName("AclTest2");
        acl = new ClazzAcl();
        acl.setAccess(ClazzAclMethod.FIND, user1Id, true);
        acl.setAccess(ClazzAclMethod.INSERT, user1Id, true);
        acl.setAccess(ClazzAclMethod.DELETE, user2Id, true);
        acl.setAccess(ClazzAclMethod.UPDATE, user2Id, true);
        clazz2.setAcl(acl);
        clazzService.insert(app.getId(), clazz2);
        fieldString = new Field(FieldType.STRING, "string");
        fieldService.insert(app.getId(), "AclTest2", fieldString);
        t = new BaasObject();
        t.put("string", "string");
        objectService.insert(app.getId(), "cloud", "AclTest2", t, null, true);

        //创建测试用类3 可get 禁止find
        Clazz clazz3 = new Clazz();
        clazz3.setName("AclTest3");
        acl = new ClazzAcl();
        acl.setPublicAccess(ClazzAclMethod.GET, true);
        acl.setPublicAccess(ClazzAclMethod.FIND, false);
        clazz3.setAcl(acl);
        clazzService.insert(app.getId(), clazz3);
        fieldString = new Field(FieldType.STRING, "string");
        fieldService.insert(app.getId(), "AclTest3", fieldString);

        //创建测试用类 全局可读可写
        Clazz objectAcl = new Clazz();
        objectAcl.setName("objectAcl");
        clazzService.insert(app.getId(), objectAcl);
        fieldString = new Field(FieldType.STRING, "string");
        fieldService.insert(app.getId(), "objectAcl", fieldString);

    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    /**
     * 测试master权限
     *
     * @throws SimpleError
     */
    @Test
    public void testMaster() {
        //可以查询
        List<BaasObject> result = objectService.find(app.getId(), "admin", "AclMaster", null, null, null, null, 100, 0, null, true);
        Assert.assertThat(result.size(), equalTo(1));
    }

    /**
     * 测试表级ACL
     *
     * @throws SimpleError
     */
    @Test
    public void testClazz1Acl() {
        //可以查询
        List<BaasObject> result = objectService.find(app.getId(), "admin", "AclTest1", null, null, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1));
        //禁止更新
        BaasObject t = result.get(0);
        t.put("string", "test");
        String id = t.getId();
        try {
            objectService.update(app.getId(), "admin", "AclTest1", id, t, null, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_CLAZZ_NO_ACCESS.getCode()));
        }
        //禁止删除
        try {
            objectService.delete(app.getId(), "admin", "AclTest1", id, null, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_CLAZZ_NO_ACCESS.getCode()));
        }
        //禁止写入
        t = new BaasObject();
        try {
            objectService.insert(app.getId(), "cloud", "AclTest1", t, null, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_CLAZZ_NO_ACCESS.getCode()));
        }
    }

    /**
     * 测试表级ACL
     *
     * @throws SimpleError
     */
    @Test
    public void testClazz2Acl() {
        BaasUser user1 = userService.get(app.getId(), "admin", "user1", null, true);
        BaasUser user2 = userService.get(app.getId(), "admin", "user2", null, true);
        //user1有读权限
        List<BaasObject> result = objectService.find(app.getId(), "admin", "AclTest2", null, null, null, null, 100, 0, user1, false);
        //已存在的对象
        BaasObject t1 = result.get(0);
        Assert.assertThat(result.size(), equalTo(1));
        //user2无读权限
        try {
            objectService.find(app.getId(), "admin", "AclTest2", null, null, null, null, 100, 0, user2, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_CLAZZ_NO_ACCESS.getCode()));
        }
        //user1有写权限
        BaasObject t = new BaasObject();
        objectService.insert(app.getId(), "cloud", "AclTest2", t, user1, false);
        //user2无写权限
        try {
            objectService.insert(app.getId(), "cloud", "AclTest2", t, user2, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_CLAZZ_NO_ACCESS.getCode()));
        }
        //user1无改权限
        try {
            objectService.update(app.getId(), "admin", "AclTest2", t1.getId(), t, user1, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_CLAZZ_NO_ACCESS.getCode()));
        }
        //user2有改权限
        objectService.update(app.getId(), "admin", "AclTest2", t1.getId(), t, user2, false);
        //user1无删除权限
        try {
            objectService.delete(app.getId(), "admin", "AclTest2", t1.getId(), user1, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_CLAZZ_NO_ACCESS.getCode()));
        }
        //user2有删除权限
        objectService.delete(app.getId(), "admin", "AclTest2", t1.getId(), user2, false);
    }

    /**
     * 测试表级权限 允许get 禁止find
     */
    @Test
    public void testClazzFindAcl() {
        BaasObject t = new BaasObject();
        t.put("string", "string");
        t = objectService.insert(app.getId(), "cloud", "AclTest3", t, null, true);
        //测试get方法可用
        BaasObject obj = objectService.get(app.getId(), "admin", "AclTest3", t.getId(), null, null, null, false);
        Assert.assertThat(obj, not(nullValue()));
        Assert.assertThat(obj.getString("string"), equalTo("string"));

        //测试find方法被拒绝
        try {
            objectService.find(app.getId(), "admin", "AclTest3", new BaasQuery(), null, null, null, 0, 0, null, false);
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_CLAZZ_NO_ACCESS.getCode()));
        }
    }

    /**
     * 测试对象级ACL
     *
     * @throws SimpleError
     */
    @Test
    public void testObjectAclWithUser() {
        BaasUser user1 = userService.get(app.getId(), "admin", "user1", null, true);
        BaasUser user2 = userService.get(app.getId(), "admin", "user2", null, true);
        //测试对象 全局可读 user1可写
        BaasObject o1 = new BaasObject();
        o1.put("string", "old");
        BaasAcl acl = new BaasAcl();
        acl.setPublicReadAccess(true);
        acl.setWriteAccess(user1, true);
        o1.setAcl(acl);
        String o1Id = objectService.insert(app.getId(), "cloud", "objectAcl", o1, null, false).getId();

        o1 = objectService.get(app.getId(), "admin", "objectAcl", o1Id, null, null, null, false);
        Assert.assertThat(o1.get("string"), equalTo("old"));
        o1.put("string", "new");
        //user2无改权限
        try {
            objectService.update(app.getId(), "admin", "objectAcl", o1.getId(), o1, user2, false);
            Assert.fail("user2无修改权限");
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_NO_ACCESS.getCode()));
        }
        //user1有改权限
        objectService.update(app.getId(), "admin", "objectAcl", o1.getId(), o1, user1, false);
        o1 = objectService.get(app.getId(), "admin", "objectAcl", o1Id, null, null, null, false);
        Assert.assertThat(o1.get("string"), equalTo("new"));
        //master权限 可以修改
        o1.put("string", "master");
        objectService.update(app.getId(), "admin", "objectAcl", o1.getId(), o1, null, true);
        o1 = objectService.get(app.getId(), "admin", "objectAcl", o1Id, null, null, null, false);
        Assert.assertThat(o1.get("string"), equalTo("master"));

        //测试对象 user1可读
        BaasObject o2 = new BaasObject();
        o2.put("string", "old");
        acl = new BaasAcl();
        acl.setReadAccess(user1, true);
        acl.setPublicWriteAccess(false);
        o2.setAcl(acl);
        String o2Id = objectService.insert(app.getId(), "cloud", "objectAcl", o2, null, false).getId();

        //无用户身份 无法获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2Id, null, null, null, false);
        Assert.assertThat(o2, equalTo(null));
        //无用户身份 无法查询到o2 可以查询到o1
        List<BaasObject> objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, null, 100, 0, null, false);
        Assert.assertThat(objects.size(), equalTo(1));
        Assert.assertThat(objects.get(0).getId(), equalTo(o1Id));

        //user2 无法获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2Id, null, null, user2, false);
        Assert.assertThat(o2, equalTo(null));
        //user2 无法查询到o2 可以查询到o1
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, null, 100, 0, user2, false);
        Assert.assertThat(objects.size(), equalTo(1));
        Assert.assertThat(objects.get(0).getId(), equalTo(o1Id));

        //user1 可以获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2Id, null, null, user1, false);
        Assert.assertThat(o2.get("string"), equalTo("old"));
        //user1 可以查询到o2 可以查询到o1
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, null, 100, 0, user1, false);
        Assert.assertThat(objects.size(), equalTo(2));
        Assert.assertThat(objects.get(0).getId(), equalTo(o2Id));

        //master权限 可以获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2Id, null, null, null, true);
        Assert.assertThat(o2.get("string"), equalTo("old"));
        //master权限 可以查询到o2 可以查询到o1
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, null, 100, 0, null, true);
        Assert.assertThat(objects.size(), equalTo(2));
        Assert.assertThat(objects.get(0).getId(), equalTo(o2Id));
    }

    @Test
    public void testObjectAclWithRole() {
        BaasUser user1 = userService.get(app.getId(), "admin", "user1", null, true);
        BaasUser user2 = userService.get(app.getId(), "admin", "user2", null, true);
        BaasUser user3 = userService.get(app.getId(), "admin", "user3", null, true);

        BaasRole role1 = roleService.get(app.getId(),"cloud", "role1", null, true);
        BaasRole role2 = roleService.get(app.getId(),"cloud", "role2", null, true);
        BaasRole role3 = roleService.get(app.getId(),"cloud", "role3", null, true);
        BaasRole role4 = roleService.get(app.getId(),"cloud", "role4", null, true);

        // 全局可读, 角色1可写
        BaasObject o1 = new BaasObject();
        o1.put("string", "old");
        BaasAcl acl = new BaasAcl();
        acl.setPublicReadAccess(true);
        acl.setWriteAccess(role1, true);
        o1.setAcl(acl);
        o1 = objectService.insert(app.getId(), "cloud", "objectAcl", o1, null, false);
        o1 = objectService.get(app.getId(), "plat", "objectAcl", o1.getId(), null, null, null, true);
        Assert.assertThat(o1.get("string"), equalTo("old"));
        o1.put("string", "new");
        // user2无改权限
        try {
            objectService.update(app.getId(), "admin", "objectAcl", o1.getId(), o1, user2, false);
            Assert.fail("user2无修改权限");
        } catch (SimpleError e) {
            Assert.assertThat(e.getCode(), equalTo(SimpleCode.OBJECT_NO_ACCESS.getCode()));
        }
        // user1有改权限
        objectService.update(app.getId(), "admin", "objectAcl", o1.getId(), o1, user1, false);
        o1 = objectService.get(app.getId(), "admin", "objectAcl", o1.getId(), null, null, null, true);
        Assert.assertThat(o1.get("string"), equalTo("new"));
        // master权限可以修改
        o1.put("string", "master");
        objectService.update(app.getId(), "admin", "objectAcl", o1.getId(), o1, null, true);
        o1 = objectService.get(app.getId(), "admin", "objectAcl", o1.getId(), null, null, null, true);
        Assert.assertThat(o1.get("string"), equalTo("master"));

        // 测试对象 role1可读
        BaasObject o2 = new BaasObject();
        o2.put("string", "old");
        acl = new BaasAcl();
        acl.setReadAccess(user1, true);
        o2.setAcl(acl);
        o2 = objectService.insert(app.getId(), "admin", "objectAcl", o2, null, false);

        // 无用户身份无法获取到o2
        BaasObject object = objectService.get(app.getId(), "admin", "objectAcl", o2.getId(), null, null, null, false);
        Assert.assertThat(object, equalTo(null));
        // 无用户身份可以查询到o1,无法查询到o2
        List<BaasObject>  objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, null, 100, 0, null, false);
        Assert.assertThat(objects.size(), equalTo(1));
        Assert.assertThat(objects.get(0).getId(), equalTo(o1.getId()));

        // user2无法获取到o2
        object = objectService.get(app.getId(),"admin", "objectAcl", o2.getId(), null, null, user2, false);
        Assert.assertThat(object, equalTo(null));
        // user2可以查询到o1,无法查询到o2
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, null, 100, 0, user2, false);
        Assert.assertThat(objects.size(), equalTo(1));
        Assert.assertThat(objects.get(0).getId(), equalTo(o1.getId()));

        // user1可以获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2.getId(), null, null, user1, false);
        Assert.assertThat(o2.get("string"), equalTo("old"));
        //user1 可以查询到o2 可以查询到o1
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, null, 100, 0, user1, false);
        Assert.assertThat(objects.size(), equalTo(2));
        List<String> ids = new ArrayList<>();
        objects.forEach(o -> ids.add(o.getId()));
        Assert.assertTrue("user2不能同时查询到o1和o2", ids.contains(o1.getId()) && ids.contains(o2.getId()));

        // master权限 可以获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2.getId(), null, null, null, true);
        Assert.assertThat(o2.get("string"), equalTo("old"));
        //master权限 可以查询到o2 可以查询到o1
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, null, 100, 0, null, true);
        Assert.assertThat(objects.size(), equalTo(2));
        Assert.assertTrue("master不能同时查询到o1和o2", ids.contains(o1.getId()) && ids.contains(o2.getId()));

        // 测试对象 角色3可写
        BaasObject o3 = new BaasObject();
        o3.put("string", "old");
        acl = new BaasAcl();
        acl.setPublicReadAccess(true);
        acl.setWriteAccess(role3, true);
        o3.setAcl(acl);
        o3 = objectService.insert(app.getId(), "admin", "objectAcl", o3, null, false);

        // user2 无法修改o3
        o3.put("string", "user2write");
        try {
            objectService.update(app.getId(), "admin", "objectAcl", o3.getId(), o3, user2, false);
            Assert.fail("user2无修改权限");
        } catch (SimpleError e) {
            Assert.assertThat(e.getCode(), equalTo(SimpleCode.OBJECT_NO_ACCESS.getCode()));
        }
        // user1 可以修改o3
        o3.put("string", "user1write");
        objectService.update(app.getId(), "admin", "objectAcl", o3.getId(), o3, user1, false);
        o3 = objectService.get(app.getId(), "admin", "objectAcl", o3.getId(), null, null, null, true);
        Assert.assertThat(o3.get("string"), equalTo("user1write"));

        // 测试对象 角色4可以读取和修改
        BaasObject o4 = new BaasObject();
        o4.put("string", "old");
        acl = new BaasAcl();
        acl.setReadAccess(role4, true);
        acl.setWriteAccess(role4, true);
        o4.setAcl(acl);
        o4 = objectService.insert(app.getId(), "admin", "objectAcl", o4, null, false);

        // user2无法读取和修改o4
        object = objectService.get(app.getId(), "admin", "objectAcl", o4.getId(), null, null, user2, false);
        Assert.assertThat(object, equalTo(null));
        o4.put("string", "user2write");
        try {
            objectService.update(app.getId(), "admin", "objectAcl", o4.getId(), o4, user2, false);
            Assert.fail("user2无修改权限");
        } catch (SimpleError e) {
            Assert.assertThat(e.getCode(), equalTo(SimpleCode.OBJECT_NO_ACCESS.getCode()));
        }
        // user1可以获取和修改o4
        object = objectService.get(app.getId(), "admin", "objectAcl", o4.getId(), null, null, user1, false);
        Assert.assertThat(object.getId(), equalTo(o4.getId()));
        o4.put("string", "user1write");
        objectService.update(app.getId(), "admin", "objectAcl", o4.getId(), o4, user1, false);
        o4 = objectService.get(app.getId(), "admin", "objectAcl", o4.getId(), null, null, null, true);
        Assert.assertThat(o4.get("string"), equalTo("user1write"));
        // user3可以获取和修改o4
        object = objectService.get(app.getId(), "admin", "objectAcl", o4.getId(), null, null, user3, false);
        Assert.assertThat(object.getId(), equalTo(o4.getId()));
        o4.put("string", "user3write");
        objectService.update(app.getId(), "admin", "objectAcl", o4.getId(), o4, user3, false);
        o4 = objectService.get(app.getId(), "admin", "objectAcl", o4.getId(), null, null, null, true);
        Assert.assertThat(o4.get("string"), equalTo("user3write"));

    }

}
