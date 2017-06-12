package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.entity.Field;
import com.javabaas.server.admin.entity.FieldType;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by Staryet on 15/8/11.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class,webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class FieldTests {

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
        app.setName("FieldTestApp");
        appService.insert(app);

        Clazz clazz = new Clazz("Test");
        clazzService.insert(app.getId(), clazz);
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    /**
     * 测试字段的创建
     *
     * @throws SimpleError
     */
    @Test
    public void testInsert() {
        Field fieldString = new Field(FieldType.STRING, "string");
        fieldService.insert(app.getId(), "Test", fieldString);

        Field field = fieldService.get(app.getId(), "Test", "string");
        Assert.assertThat(field, notNullValue());

        //测试禁止使用保留字创建Field
        Field fieldError = new Field(FieldType.STRING, "_aaa");
        try {
            fieldService.insert(app.getId(), "Test", fieldError);
            Assert.fail("禁止使用保留子创建字段");
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.FIELD_NAME_ERROR.getCode()));
        }
    }


}
