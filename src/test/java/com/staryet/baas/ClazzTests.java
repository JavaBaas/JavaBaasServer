package com.staryet.baas;

import com.staryet.baas.admin.entity.App;
import com.staryet.baas.admin.entity.Clazz;
import com.staryet.baas.admin.entity.Field;
import com.staryet.baas.admin.entity.FieldType;
import com.staryet.baas.admin.service.AppService;
import com.staryet.baas.admin.service.ClazzService;
import com.staryet.baas.admin.service.FieldService;
import com.staryet.baas.common.entity.SimpleCode;
import com.staryet.baas.common.entity.SimpleError;
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

/**
 * Created by Staryet on 15/8/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest("server.port:9000")
public class ClazzTests {

    @Autowired
    private AppService appService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private FieldService fieldService;

    private App app;

    @Before
    public void before() {
        app = new App();
        app.setName("ClazzTest");
        appService.insert(app);
        app = appService.get(app.getId());
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testInsert() {
        Clazz clazz1 = new Clazz();
        clazz1.setName("Clazz1");
        clazz1.setApp(app);
        clazzService.insert(app.getId(), clazz1);
        clazz1 = clazzService.get(app.getId(), "Clazz1");
        Assert.assertThat(clazz1.getName(), equalTo("Clazz1"));

        //测试类名称错误
        Clazz clazz2 = new Clazz();
        clazz2.setName("_Clazz2");
        clazz2.setApp(app);
        try {
            clazzService.insert(app.getId(), clazz2);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.CLAZZ_NAME_ERROR.getCode()));
        }
    }

    @Test
    public void testDelete() {
        //创建类
        Clazz clazzForDelete = new Clazz();
        clazzForDelete.setName("ClazzForDelete");
        clazzForDelete.setApp(app);
        clazzService.insert(app.getId(), clazzForDelete);
        clazzForDelete = clazzService.get(app.getId(), "ClazzForDelete");
        //添加字段
        fieldService.insert(app.getId(), "ClazzForDelete", new Field(FieldType.STRING, "field1"));
        fieldService.insert(app.getId(), "ClazzForDelete", new Field(FieldType.STRING, "field2"));
        fieldService.insert(app.getId(), "ClazzForDelete", new Field(FieldType.STRING, "field3"));
        //验证创建成功
        Assert.assertThat(clazzForDelete.getName(), equalTo("ClazzForDelete"));
        List<Field> fields = fieldService.list(app.getId(), "ClazzForDelete");
        Assert.assertThat(fields.size(), equalTo(3));

        //删除类
        clazzService.delete(app.getId(), "ClazzForDelete");

        //验证删除成功
        try {
            clazzService.get(app.getId(), "ClazzForDelete");
            Assert.fail();
        } catch (SimpleError e) {
            Assert.assertThat(e.getCode(), equalTo(SimpleCode.CLAZZ_NOT_FOUND.getCode()));
        }
    }

}
