package com.javabaas.server;

import com.javabaas.server.admin.entity.dto.FieldExport;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.user.service.UserService;
import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.entity.Field;
import com.javabaas.server.admin.entity.FieldType;
import com.javabaas.server.admin.entity.dto.AppExport;
import com.javabaas.server.admin.entity.dto.ClazzExport;
import com.javabaas.server.admin.repository.ClazzRepository;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.object.service.ObjectService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Created by Staryet on 15/8/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest("server.port:9000")
public class AppTests {

    @Autowired
    private AppService appService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private UserService userService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private ClazzRepository clazzRepository;

    private App app;

    @Before
    public void before() {
        app = new App();
        app.setName("AppTest1");
        appService.insert(app);
        app = appService.get(app.getId());
        //创建测试用类
        Clazz clazz1 = new Clazz("clazz1");
        clazzService.insert(app.getId(), clazz1);
        //创建测试用字段
        Field field1 = new Field(FieldType.STRING, "field1");
        Field field2 = new Field(FieldType.NUMBER, "field2");
        fieldService.insert(app.getId(), "clazz1", field1);
        fieldService.insert(app.getId(), "clazz1", field2);
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testApp() {
        //验证应用名称
        Assert.assertThat(app.getName(), equalTo("AppTest1"));
        //验证系统内建类
        Clazz userClazz = clazzService.get(app.getId(), UserService.USER_CLASS_NAME);
        Assert.assertThat(userClazz.getName(), equalTo(UserService.USER_CLASS_NAME));
    }

    public void testDelete() {
        App appForDelete = new App();
        appForDelete.setName("AppForDelete");
        appService.insert(appForDelete);
        appForDelete = appService.get(appForDelete.getId());
        clazzService.insert(appForDelete.getId(), new Clazz("Clazz1"));
        clazzService.insert(appForDelete.getId(), new Clazz("Clazz2"));
        clazzService.insert(appForDelete.getId(), new Clazz("Clazz3"));
        //验证创建成功
        Assert.assertThat(appForDelete.getName(), equalTo("AppForDelete"));
        List<Clazz> clazz = clazzService.list(appForDelete.getId());
        Assert.assertThat(clazz.size(), equalTo(3));

        //删除
        appService.delete(appForDelete.getId());
        //检查是否清除干净
        appForDelete = appService.get(appForDelete.getId());
        Assert.assertThat(appForDelete, nullValue());
        //相关类已删除
        clazz = clazzService.list(appForDelete.getId());
        Assert.assertThat(clazz.size(), equalTo(0));
    }

    /**
     * 验证应用禁止重名
     *
     * @throws SimpleError
     */
    @Test
    public void testDuplicate() {
        App appDuplicate = new App();
        appDuplicate.setName("AppTest1");
        try {
            appService.insert(appDuplicate);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.APP_ALREADY_EXIST.getCode()));
        }
    }

    @Test
    public void testExport() {
        AppExport appExport = appService.export(app.getId());
        Assert.assertThat(appExport.getName(), equalTo("AppTest1"));
        Assert.assertThat(appExport.getClazzs().size(), equalTo(5));
        ClazzExport clazz1 = appExport.getClazzs().get(4);
        Assert.assertThat(clazz1.getName(), equalTo("clazz1"));
        Assert.assertThat(clazz1.getFields().size(), equalTo(2));
        FieldExport field1 = clazz1.getFields().get(0);
        FieldExport field2 = clazz1.getFields().get(1);
        Assert.assertThat(field1.getName(), equalTo("field1"));
        Assert.assertThat(field1.getType(), equalTo(FieldType.STRING));
        Assert.assertThat(field2.getName(), equalTo("field2"));
        Assert.assertThat(field2.getType(), equalTo(FieldType.NUMBER));
    }

    @Test
    public void testImport() {
        AppExport appExport = appService.export(app.getId());
        appExport.setName("AppTest2");
        App app2 = appService.importData(appExport);

        //验证导入结果
        appExport = appService.export(app2.getId());
        Assert.assertThat(appExport.getName(), equalTo("AppTest2"));
        Assert.assertThat(appExport.getClazzs().size(), equalTo(5));
        ClazzExport clazz1 = appExport.getClazzs().get(4);
        Assert.assertThat(clazz1.getName(), equalTo("clazz1"));
        Assert.assertThat(clazz1.getFields().size(), equalTo(2));
        FieldExport field1 = clazz1.getFields().get(0);
        FieldExport field2 = clazz1.getFields().get(1);
        Assert.assertThat(field1.getName(), equalTo("field1"));
        Assert.assertThat(field1.getType(), equalTo(FieldType.STRING));
        Assert.assertThat(field2.getName(), equalTo("field2"));
        Assert.assertThat(field2.getType(), equalTo(FieldType.NUMBER));

        appService.delete(app2.getId());
    }

}
