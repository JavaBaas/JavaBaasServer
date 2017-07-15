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
        List<BaasObject> result = objectService.find(app.getId(), "admin", "AclMaster", null, null, null, 100, 0, null, true);
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
        List<BaasObject> result = objectService.find(app.getId(), "admin", "AclTest1", null, null, null, 100, 0, null, false);
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
        List<BaasObject> result = objectService.find(app.getId(), "admin", "AclTest2", null, null, null, 100, 0, user1, false);
        //已存在的对象
        BaasObject t1 = result.get(0);
        Assert.assertThat(result.size(), equalTo(1));
        //user2无读权限
        try {
            objectService.find(app.getId(), "admin", "AclTest2", null, null, null, 100, 0, user2, false);
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
        BaasObject obj = objectService.get(app.getId(), "admin", "AclTest3", t.getId(), null, null, false);
        Assert.assertThat(obj, not(nullValue()));
        Assert.assertThat(obj.getString("string"), equalTo("string"));

        //测试find方法被拒绝
        try {
            objectService.find(app.getId(), "admin", "AclTest3", new BaasQuery(), null, null, 0, 0, null, false);
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
    public void testObjectAcl() {
        BaasUser user1 = userService.get(app.getId(), "admin", "user1", null, true);
        BaasUser user2 = userService.get(app.getId(), "admin", "user2", null, true);
        //测试对象 全局可读 user1可写
        BaasObject o1 = new BaasObject();
        o1.put("string", "old");
        BaasAcl acl = new BaasAcl();
        acl.setPublicReadAccess(true);
        acl.setWriteAccess(user1.getId(), true);
        o1.setAcl(acl);
        String o1Id = objectService.insert(app.getId(), "cloud", "objectAcl", o1, null, false).getId();

        o1 = objectService.get(app.getId(), "admin", "objectAcl", o1Id, null, null, false);
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
        o1 = objectService.get(app.getId(), "admin", "objectAcl", o1Id, null, null, false);
        Assert.assertThat(o1.get("string"), equalTo("new"));
        //master权限 可以修改
        o1.put("string", "master");
        objectService.update(app.getId(), "admin", "objectAcl", o1.getId(), o1, null, true);
        o1 = objectService.get(app.getId(), "admin", "objectAcl", o1Id, null, null, false);
        Assert.assertThat(o1.get("string"), equalTo("master"));

        //测试对象 user1可读
        BaasObject o2 = new BaasObject();
        o2.put("string", "old");
        acl = new BaasAcl();
        acl.setReadAccess(user1.getId(), true);
        acl.setPublicWriteAccess(false);
        o2.setAcl(acl);
        String o2Id = objectService.insert(app.getId(), "cloud", "objectAcl", o2, null, false).getId();

        //无用户身份 无法获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2Id, null, null, false);
        Assert.assertThat(o2, equalTo(null));
        //无用户身份 无法查询到o2 可以查询到o1
        List<BaasObject> objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, 100, 0, null, false);
        Assert.assertThat(objects.size(), equalTo(1));
        Assert.assertThat(objects.get(0).getId(), equalTo(o1Id));

        //user2 无法获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2Id, null, user2, false);
        Assert.assertThat(o2, equalTo(null));
        //user2 无法查询到o2 可以查询到o1
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, 100, 0, user2, false);
        Assert.assertThat(objects.size(), equalTo(1));
        Assert.assertThat(objects.get(0).getId(), equalTo(o1Id));

        //user1 可以获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2Id, null, user1, false);
        Assert.assertThat(o2.get("string"), equalTo("old"));
        //user1 可以查询到o2 可以查询到o1
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, 100, 0, user1, false);
        Assert.assertThat(objects.size(), equalTo(2));
        Assert.assertThat(objects.get(0).getId(), equalTo(o2Id));

        //master权限 可以获取到o2
        o2 = objectService.get(app.getId(), "admin", "objectAcl", o2Id, null, null, true);
        Assert.assertThat(o2.get("string"), equalTo("old"));
        //master权限 可以查询到o2 可以查询到o1
        objects = objectService.find(app.getId(), "admin", "objectAcl", null, null, null, 100, 0, null, true);
        Assert.assertThat(objects.size(), equalTo(2));
        Assert.assertThat(objects.get(0).getId(), equalTo(o2Id));


    }

}
