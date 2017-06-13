package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.entity.Field;
import com.javabaas.server.admin.entity.FieldType;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.cloud.entity.CloudSetting;
import com.javabaas.server.cloud.entity.HookSetting;
import com.javabaas.server.cloud.service.CloudService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.user.entity.BaasUser;
import com.javabaas.server.user.service.UserService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Staryet on 15/8/11.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HookTests {

    @Autowired
    private AppService appService;
    @Autowired
    private CloudService cloudService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private UserService userService;
    @Autowired
    private FieldService fieldService;
    @LocalServerPort
    private String port;

    private App app;

    @Before
    public void before() {
        app = new App();
        app.setName("AppHook");
        appService.insert(app);
        app = appService.get(app.getId());
        //创建用于测试的类
        Clazz book = new Clazz("Book");
        clazzService.insert(app.getId(), book);
        //创建字段
        Field titleField = new Field(FieldType.STRING, "title");
        Field priceField = new Field(FieldType.NUMBER, "price");
        fieldService.insert(app.getId(), "Book", titleField);
        fieldService.insert(app.getId(), "Book", priceField);
        //部署钩子
        CloudSetting cloudSetting = new CloudSetting();
        cloudSetting.setCustomerHost("http://127.0.0.1:" + port + "/customer/");
        Map<String, HookSetting> hookSettings = new HashMap<>();
        HookSetting bookHook = new HookSetting(true);
        hookSettings.put("Book", bookHook);
        cloudSetting.setHookSettings(hookSettings);
        cloudService.deploy(app.getId(), cloudSetting);
        //用户
        BaasUser user = new BaasUser();
        user.setUsername("user");
        user.setPassword("aaaaaa");
        userService.register(app.getId(), "cloud", user);
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testInsert() {
        //测试插入对象用户信息
        BaasUser user = userService.get(app.getId(), "admin", "user", null, true);
        BaasObject book = new BaasObject();
        book.put("title", "ThreeBody");
        book.put("price", 888);
        objectService.insert(app.getId(), "cloud", "Book", book, user, false);
        //测试钩子中断
        book = new BaasObject();
        book.put("title", "ForeBody");
        book.put("price", 888);
        try {
            objectService.insert(app.getId(), "cloud", "Book", book, user, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.HOOK_INTERCEPTION.getCode()));
        }
        book = new BaasObject();
        book.put("title", "ThreeBody");
        book.put("price", 1);
        try {
            objectService.insert(app.getId(), "cloud", "Book", book, user, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.HOOK_INTERCEPTION.getCode()));
        }

        //测试在beforeInsert中改变数据内容
        book = new BaasObject();
        book.put("title", "TwoBody");
        book.put("price", 10);
        String id = objectService.insert(app.getId(), "cloud", "Book", book, user, false).getId();
        book = objectService.get(app.getId(), "admin", "Book", id);
        Assert.assertThat(book.get("title"), equalTo("MineBody"));
        Assert.assertThat(book.get("price"), equalTo(100));

        //测试在afterInsert中改变数据内容
        book = new BaasObject();
        book.put("title", "StarWars");
        book.put("price", 10);
        id = objectService.insert(app.getId(), "cloud", "Book", book, user, false).getId();
        book = objectService.get(app.getId(), "admin", "Book", id);
        Assert.assertThat(book.get("title"), equalTo("Obiwan"));
        Assert.assertThat(book.get("price"), equalTo(100));
    }

    @Test
    public void testUpdate() {
        BaasUser user = userService.get(app.getId(), "admin", "user", null, true);
        BaasObject book = new BaasObject();
        book.put("title", "ThreeBody");
        book.put("price", 888);
        String id = objectService.insert(app.getId(), "cloud", "Book", book, user, false).getId();

        //验证插入成功
        book = objectService.get(app.getId(), "admin", "Book", id);
        Assert.assertThat(book.get("title"), equalTo("ThreeBody"));
        Assert.assertThat(book.get("price"), equalTo(888));

        //测试钩子中断
        book.put("title", "ForeBody");
        book.put("price", 888);
        try {
            objectService.update(app.getId(), "admin", "Book", id, book, user, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.HOOK_INTERCEPTION.getCode()));
        }
        book = new BaasObject();
        book.put("title", "ThreeBody");
        book.put("price", 1);
        try {
            objectService.update(app.getId(), "admin", "Book", id, book, user, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.HOOK_INTERCEPTION.getCode()));
        }

        //验证数据未改变
        book = objectService.get(app.getId(), "admin", "Book", id);
        Assert.assertThat(book.get("title"), equalTo("ThreeBody"));
        Assert.assertThat(book.get("price"), equalTo(888));

        //测试在beforeUpdate中改变数据内容
        book = new BaasObject();
        book.put("title", "TwoBody");
        book.put("price", 10);
        objectService.update(app.getId(), "admin", "Book", id, book, user, false);
        book = objectService.get(app.getId(), "admin", "Book", id);
        Assert.assertThat(book.get("title"), equalTo("MineBody"));
        Assert.assertThat(book.get("price"), equalTo(100));
    }

    @Test
    public void testDelete() {
        BaasUser user = userService.get(app.getId(), "admin", "user", null, true);
        BaasObject book = new BaasObject();
        book.put("title", "ThreeBody");
        book.put("price", 888);
        String id = objectService.insert(app.getId(), "cloud", "Book", book, user, false).getId();

        //验证插入成功
        book = objectService.get(app.getId(), "admin", "Book", id);
        Assert.assertThat(book.get("title"), equalTo("ThreeBody"));
        Assert.assertThat(book.get("price"), equalTo(888));

        //测试钩子中断
        try {
            objectService.delete(app.getId(), "admin", "Book", id, user, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.HOOK_INTERCEPTION.getCode()));
        }
    }


}
