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
import com.staryet.baas.common.util.JSONUtil;
import com.staryet.baas.object.entity.*;
import com.staryet.baas.object.service.ObjectService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.hamcrest.Matchers.*;

/**
 * 对象测试
 * Created by Staryet on 15/8/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest("server.port:9000")
public class ObjectTests {

    @Autowired
    private ClazzService clazzService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private AppService appService;
    @Autowired
    private JSONUtil jsonUtil;

    private App app;

    @Before
    public void before() {
        app = new App();
        app.setName("ObjectTestApp");
        appService.insert(app);
        //创建测试用类
        Clazz clazz = new Clazz();
        clazz.setName("ObjectTest");
        clazzService.insert(app.getId(), clazz);
        Field fieldString = new Field(FieldType.STRING, "string");
        Field fieldString2 = new Field(FieldType.STRING, "string2");
        Field fieldNumber = new Field(FieldType.NUMBER, "number");
        Field fieldBoolean = new Field(FieldType.BOOLEAN, "boolean");
        Field fieldDate = new Field(FieldType.DATE, "date");
        Field fieldObject = new Field(FieldType.OBJECT, "object");
        Field fieldArray = new Field(FieldType.ARRAY, "array");
        Field fieldPointer = new Field(FieldType.POINTER, "p");
        fieldService.insert(app.getId(), "ObjectTest", fieldString);
        fieldService.insert(app.getId(), "ObjectTest", fieldString2);
        fieldService.insert(app.getId(), "ObjectTest", fieldNumber);
        fieldService.insert(app.getId(), "ObjectTest", fieldBoolean);
        fieldService.insert(app.getId(), "ObjectTest", fieldDate);
        fieldService.insert(app.getId(), "ObjectTest", fieldObject);
        fieldService.insert(app.getId(), "ObjectTest", fieldArray);
        fieldService.insert(app.getId(), "ObjectTest", fieldPointer);

        //测试包含所需要的类
        clazz = new Clazz();
        clazz.setName("A");
        clazzService.insert(app.getId(), clazz);

        Field b = new Field(FieldType.POINTER, "b");
        fieldService.insert(app.getId(), "A", b);

        clazz = new Clazz();
        clazz.setName("B");
        clazzService.insert(app.getId(), clazz);

        Field c = new Field(FieldType.POINTER, "c");
        fieldService.insert(app.getId(), "A", c);

        Field b_c = new Field(FieldType.POINTER, "c");
        fieldService.insert(app.getId(), "B", c);

        clazz = new Clazz();
        clazz.setName("C");
        clazzService.insert(app.getId(), clazz);

        Field d = new Field(FieldType.POINTER, "d");
        fieldService.insert(app.getId(), "C", d);

        Field e = new Field(FieldType.POINTER, "e");
        fieldService.insert(app.getId(), "C", e);

        clazz = new Clazz();
        clazz.setName("D");
        clazzService.insert(app.getId(), clazz);

        Field f = new Field(FieldType.POINTER, "f");
        fieldService.insert(app.getId(), "D", f);

        clazz = new Clazz();
        clazz.setName("E");
        clazzService.insert(app.getId(), clazz);
        clazz = new Clazz();
        clazz.setName("F");
        clazzService.insert(app.getId(), clazz);

        Field name = new Field(FieldType.STRING, "name");
        fieldService.insert(app.getId(), "A", name);
        fieldService.insert(app.getId(), "B", name);
        fieldService.insert(app.getId(), "C", name);
        fieldService.insert(app.getId(), "D", name);
        fieldService.insert(app.getId(), "E", name);
        fieldService.insert(app.getId(), "F", name);

    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    /**
     * 测试插入对象
     *
     * @throws SimpleError
     */
    @Test
    public void testInsert() {
        BaasObject t = new BaasObject();
        t.put("string", "string");
        t.put("number", 100);
        t.put("boolean", true);
        Date date = new Date();
        t.put("date", date.getTime());
        BaasObject object = new BaasObject();
        object.put("test", "test");
        t.put("object", object);
        BaasList list = new BaasList();
        list.add("1");
        list.add("2");
        t.put("array", list);
        String id = objectService.insert(app.getId(), "cloud", "ObjectTest", t, null, false).getId();

        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        //检查字段是否存储正确
        Assert.assertThat(t.get("string"), equalTo("string"));
        Assert.assertThat(t.get("number"), equalTo(100));
        Assert.assertThat(t.get("boolean"), equalTo(true));
        Assert.assertThat(t.get("date"), equalTo(date.getTime()));
        BaasObject o = (BaasObject) t.get("object");
        Assert.assertThat(o.get("test"), equalTo("test"));

        list = (BaasList) t.get("array");
        Assert.assertThat(list.get(0), equalTo("1"));
        Assert.assertThat(list.get(1), equalTo("2"));

        BaasObject p = new BaasObject();
        p.put("__type", "Pointer");
        p.put("className", "ObjectTest");
        p.put("_id", id);
        t.put("p", p);
        t.put("object", object);
        objectService.update(app.getId(), "admin", "ObjectTest", id, t, null, false);

        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        BaasObject point = (BaasObject) t.get("p");
        //检查指针字段是否存储正确
        Assert.assertThat(point.get("__type"), equalTo("Pointer"));
        Assert.assertThat(point.get("className"), equalTo("ObjectTest"));
        Assert.assertThat(point.get("_id"), equalTo(id));
    }

    @Test
    public void testDelete() {
        BaasObject t = new BaasObject();
        t.put("string", "string");
        String id = objectService.insert(app.getId(), "cloud", "ObjectTest", t, null, false).getId();

        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        //检查对象是否保存成功
        Assert.assertThat(t.get("string"), equalTo("string"));

        //检查对象是否删除成功
        objectService.delete(app.getId(), "admin", "ObjectTest", id, null, false);
        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        Assert.assertThat(t, equalTo(null));

        //条件删除
        t = new BaasObject();
        t.put("string", "a");
        t.put("number", 1);
        t.put("string2", "deleteByQuery");
        objectService.insert(app.getId(), "cloud", "ObjectTest", t, null, false).getId();
        t = new BaasObject();
        t.put("string", "a");
        t.put("number", 2);
        t.put("string2", "deleteByQuery");
        objectService.insert(app.getId(), "cloud", "ObjectTest", t, null, false).getId();
        t = new BaasObject();
        t.put("string", "b");
        t.put("number", 1);
        t.put("string2", "deleteByQuery");
        objectService.insert(app.getId(), "cloud", "ObjectTest", t, null, false).getId();
        t = new BaasObject();
        t.put("string", "b");
        t.put("number", 2);
        t.put("string2", "deleteByQuery");
        objectService.insert(app.getId(), "cloud", "ObjectTest", t, null, false).getId();

        BaasQuery query = new BaasQuery();
        query.put("number", 2);
        objectService.deleteByQuery(app.getId(), "admin", "ObjectTest", query, null, false);

        //验证删除成功
        query = new BaasQuery();
        query.put("string2", "deleteByQuery");
        List<BaasObject> objects = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 1000, 0, null, false);
        Assert.assertThat(objects.size(), equalTo(2));
        Assert.assertThat(objects.get(0).get("number"), equalTo(1));
        Assert.assertThat(objects.get(0).get("string"), equalTo("b"));
        Assert.assertThat(objects.get(1).get("number"), equalTo(1));
        Assert.assertThat(objects.get(1).get("string"), equalTo("a"));

    }

    /**
     * 测试更新
     *
     * @throws SimpleError
     */
    @Test
    public void testUpdate() {
        BaasObject t = new BaasObject();
        t.put("string", "string");
        t.put("string2", "string2");
        t.put("number", 100);
        t.put("boolean", true);
        BaasList array = new BaasList();
        BaasObject array1 = new BaasObject();
        array1.put("string", "string");
        array1.put("number", 100);
        array.add(array1);
        t.put("array", array);
        String id = objectService.insert(app.getId(), "cloud", "ObjectTest", t, null, false).getId();

        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        //检查对象是否保存成功
        Assert.assertThat(t.get("string"), equalTo("string"));
        Assert.assertThat(t.get("string2"), equalTo("string2"));
        Assert.assertThat(t.get("number"), equalTo(100));
        Assert.assertThat(t.get("boolean"), equalTo(true));
        Assert.assertThat(t.getCreatedPlatform(), equalTo("cloud"));
        Assert.assertThat(t.getUpdatedPlatform(), equalTo("cloud"));
        array = t.getList("array");
        Object object = array.get(0);
        BaasObject a1 = new BaasObject((Map<String, Object>) object);
        Assert.assertThat(a1.getString("string"), equalTo("string"));
        Assert.assertThat(a1.getInt("number"), equalTo(100));
        long updateTime = t.getLong("updatedAt");
        long createTime = t.getLong("createdAt");

        //修改对象
        t.put("string", "codi");
        t.put("number", 250);
        t.put("boolean", false);
        array = t.getList("array");
        object = array.get(0);
        Map<String, Object> a1update = (Map<String, Object>) object;
        //在list里面的object上更新一个属性
        a1update.put("string", "string1");
        //在list里面的object上添加一个属性
        a1update.put("new", "new");
        objectService.update(app.getId(), "admin", "ObjectTest", id, t, null, false);
        //检查对象是否修改成功
        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        Assert.assertThat(t.get("string"), equalTo("codi"));
        Assert.assertThat(t.get("number"), equalTo(250));
        Assert.assertThat(t.get("boolean"), equalTo(false));
        Assert.assertThat(t.get("string2"), equalTo("string2"));
        //验证创建时间
        Assert.assertThat(t.getLong("createdAt"), equalTo(createTime));
        //验证更新时间
        Assert.assertThat(t.getLong("updatedAt"), greaterThan(updateTime));
        Assert.assertThat(t.getCreatedPlatform(), equalTo("cloud"));
        Assert.assertThat(t.getUpdatedPlatform(), equalTo("admin"));
        //验证array类型
        array = t.getList("array");
        object = array.get(0);
        a1 = new BaasObject((Map<String, Object>) object);
        Assert.assertThat(a1.getString("string"), equalTo("string1"));
        Assert.assertThat(a1.getString("new"), equalTo("new"));


        //测试抹除字段
        t = new BaasObject();
        t.put("string", "");
        t.put("number", "");
        t.put("boolean", "");
        objectService.update(app.getId(), "admin", "ObjectTest", id, t, null, false);
        //检查对象是否修改成功
        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        Assert.assertThat(t.get("string"), equalTo(null));
        Assert.assertThat(t.get("number"), equalTo(null));
        Assert.assertThat(t.get("boolean"), equalTo(null));
        Assert.assertThat(t.get("string2"), equalTo("string2"));
        //验证创建时间
        Assert.assertThat(t.getLong("createdAt"), equalTo(createTime));
        //验证更新时间
        Assert.assertThat(t.getLong("updatedAt"), greaterThan(updateTime));

        //TODO 修改ACL
    }

    /**
     * 测试原子操作
     *
     * @throws SimpleError
     */
    @Test
    public void testIncrement() {
        BaasObject t = new BaasObject();
        t.put("string", "string");
        t.put("number", 100);
        t.put("boolean", true);
        String id = objectService.insert(app.getId(), "cloud", "ObjectTest", t, null, false).getId();

        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        //检查对象是否保存成功
        Assert.assertThat(t.get("string"), equalTo("string"));
        Assert.assertThat(t.get("number"), equalTo(100));
        Assert.assertThat(t.get("boolean"), equalTo(true));
        long updateTime = t.getLong("updatedAt");
        Assert.assertThat(updateTime, not(equalTo(0)));
        long createTime = t.getLong("createdAt");
        Assert.assertThat(updateTime, not(equalTo(0)));

        //修改对象 +1
        t = new BaasObject();
        t.put("number", 1);
        objectService.increment(app.getId(), "admin", "ObjectTest", id, t, null, false);
        //检查对象是否修改成功
        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        Assert.assertThat(t.get("string"), equalTo("string"));
        Assert.assertThat(t.get("number"), equalTo(101));
        Assert.assertThat(t.get("boolean"), equalTo(true));
        //验证创建时间
        Assert.assertThat(t.getLong("createdAt"), equalTo(createTime));
        //验证更新时间
        Assert.assertThat(t.getLong("updatedAt"), greaterThan(updateTime));

        //修改对象 +5 * 10
        t = new BaasObject();
        t.put("number", 5);
        for (int i = 0; i < 10; i++) {
            objectService.increment(app.getId(), "admin", "ObjectTest", id, t, null, false);
        }
        //检查对象是否修改成功
        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        Assert.assertThat(t.get("string"), equalTo("string"));
        Assert.assertThat(t.get("number"), equalTo(151));
        Assert.assertThat(t.get("boolean"), equalTo(true));

        //修改对象 -10 * 5
        t = new BaasObject();
        t.put("number", -10);
        for (int i = 0; i < 5; i++) {
            objectService.increment(app.getId(), "admin", "ObjectTest", id, t, null, false);
        }
        //检查对象是否修改成功
        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        Assert.assertThat(t.get("string"), equalTo("string"));
        Assert.assertThat(t.get("number"), equalTo(101));
        Assert.assertThat(t.get("boolean"), equalTo(true));

        //修改对象 -100 * 10
        t = new BaasObject();
        t.put("number", -100);
        for (int i = 0; i < 10; i++) {
            objectService.increment(app.getId(), "admin", "ObjectTest", id, t, null, false);
        }
        //检查对象是否修改成功
        t = objectService.get(app.getId(), "admin", "ObjectTest", id);
        Assert.assertThat(t.get("string"), equalTo("string"));
        Assert.assertThat(t.get("number"), equalTo(-899));
        Assert.assertThat(t.get("boolean"), equalTo(true));
    }

    /**
     * 测试查询
     */
    @Test
    public void testFind() {
        //构造查询所需的数据
        BaasObject object1 = new BaasObject();
        object1.put("string", "object1");
        object1.put("string2", "object");
        object1.put("number", 100);
        object1.put("boolean", true);

        //构建一个2015-8-16 00:00的的日期
        Calendar c = new GregorianCalendar();
        c.set(Calendar.YEAR, 2015);
        c.set(Calendar.MONTH, 7);
        c.set(Calendar.DAY_OF_MONTH, 16);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        object1.put("date", c.getTime().getTime());
        String o1Id = objectService.insert(app.getId(), "cloud", "ObjectTest", object1, null, false).getId();

        BaasObject object2 = new BaasObject();
        object2.put("string", "object2");
        object2.put("string2", "object");
        object2.put("number", 200);
        object2.put("boolean", false);
        //构建一个2015-8-17 00:00的的日期
        c = new GregorianCalendar();
        c.set(Calendar.YEAR, 2015);
        c.set(Calendar.MONTH, 7);
        c.set(Calendar.DAY_OF_MONTH, 17);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        object2.put("date", c.getTime().getTime());
        String o2Id = objectService.insert(app.getId(), "cloud", "ObjectTest", object2, null, false).getId();

        BaasObject object3 = new BaasObject();
        object3.put("string", "object3");
        object3.put("number", 300);
        object3.put("boolean", false);
        String o3Id = objectService.insert(app.getId(), "cloud", "ObjectTest", object3, null, false).getId();

        //object4 各字段均为空
        BaasObject object4 = new BaasObject();
        String o4Id = objectService.insert(app.getId(), "cloud", "ObjectTest", object4, null, false).getId();

        //id查询
        object1 = objectService.get(app.getId(), "admin", "ObjectTest", o1Id);
        Assert.assertThat(object1.getId(), equalTo(o1Id));
        Assert.assertThat(object1.get("string"), equalTo("object1"));
        Assert.assertThat(object1.get("number"), equalTo(100));

        //字符串查询
        BaasQuery query = new BaasQuery();
        query.put("string", "object1");
        List<BaasObject> result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1));
        Assert.assertThat(result.get(0).getId(), equalTo(o1Id));

        query = new BaasQuery();
        query.put("string2", "object");
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(2));

        query = new BaasQuery();
        query.put("string3", "object");
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(0));

        //数值查询
        query = new BaasQuery();
        query.put("number", 100);
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1));
        Assert.assertThat(result.get(0).getId(), equalTo(o1Id));

        //大于等于200的查询
        query = new BaasQuery();
        BaasObject gte = new BaasObject();
        gte.put("$gte", 200);
        query.put("number", gte);
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(2));

        //大于200的查询
        query = new BaasQuery();
        BaasObject gt = new BaasObject();
        gt.put("$gt", 200);
        query.put("number", gt);
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1));

        //小于300的查询
        query = new BaasQuery();
        BaasObject lt = new BaasObject();
        lt.put("$lt", 300);
        query.put("number", lt);
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(2));

        //布尔查询
        query = new BaasQuery();
        query.put("boolean", true);
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1));

        query = new BaasQuery();
        query.put("boolean", false);
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(2));

        //日期查询
        //构建一个2015-8-16 12:00的的日期
        c = new GregorianCalendar();
        c.set(Calendar.YEAR, 2015);
        c.set(Calendar.MONTH, 7);
        c.set(Calendar.DAY_OF_MONTH, 16);
        c.set(Calendar.HOUR_OF_DAY, 11);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        query = new BaasQuery();
        gt = new BaasObject();
        gt.put("$gt", c.getTime().getTime());
        query.put("date", gt);
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1));

        query = new BaasQuery();
        lt = new BaasObject();
        lt.put("$lt", c.getTime().getTime());
        query.put("date", lt);
        result = objectService.list(app.getId(), "admin", "ObjectTest", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1));

    }

    /**
     * 排序测试
     *
     * @throws SimpleError
     */
    @Test
    public void testSort() throws InterruptedException {
        //构造查询所需的数据
        BaasObject object1 = new BaasObject();
        object1.put("string", "object1");
        object1.put("string2", "object");
        object1.put("number", 100);
        object1.put("boolean", true);
        String o1Id = objectService.insert(app.getId(), "cloud", "ObjectTest", object1, null, false).getId();
        Thread.sleep(1);

        BaasObject object2 = new BaasObject();
        object2.put("string", "object2");
        object2.put("string2", "object");
        object2.put("number", 200);
        object2.put("boolean", false);
        String o2Id = objectService.insert(app.getId(), "cloud", "ObjectTest", object2, null, false).getId();
        Thread.sleep(1);

        BaasObject object3 = new BaasObject();
        object3.put("string", "object3");
        object3.put("number", 300);
        object3.put("boolean", false);
        String o3Id = objectService.insert(app.getId(), "cloud", "ObjectTest", object3, null, false).getId();
        Thread.sleep(1);

        BaasObject object4 = new BaasObject();
        String o4Id = objectService.insert(app.getId(), "cloud", "ObjectTest", object4, null, false).getId();

        //测试默认排序(更新时间排序)
        List<BaasObject> result = objectService.list(app.getId(), "admin", "ObjectTest", null, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(4));
        Assert.assertThat(result.get(0).getId(), equalTo(o4Id));
        Assert.assertThat(result.get(1).getId(), equalTo(o3Id));
        Assert.assertThat(result.get(2).getId(), equalTo(o2Id));
        Assert.assertThat(result.get(3).getId(), equalTo(o1Id));

        //数值型排序
        //正序排列
        BaasSort sort = new BaasSort();
        sort.put("number", 1);
        result = objectService.list(app.getId(), "admin", "ObjectTest", null, sort, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(4));
        Assert.assertThat(result.get(0).getId(), equalTo(o4Id));
        Assert.assertThat(result.get(1).getId(), equalTo(o1Id));
        Assert.assertThat(result.get(2).getId(), equalTo(o2Id));
        Assert.assertThat(result.get(3).getId(), equalTo(o3Id));

        //倒序排列
        sort = new BaasSort();
        sort.put("number", -1);
        result = objectService.list(app.getId(), "admin", "ObjectTest", null, sort, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(4));
        Assert.assertThat(result.get(0).getId(), equalTo(o3Id));
        Assert.assertThat(result.get(1).getId(), equalTo(o2Id));
        Assert.assertThat(result.get(2).getId(), equalTo(o1Id));
        Assert.assertThat(result.get(3).getId(), equalTo(o4Id));

        //布尔排序
        sort = new BaasSort();
        sort.put("boolean", 1);
        result = objectService.list(app.getId(), "admin", "ObjectTest", null, sort, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(4));
        Assert.assertThat(result.get(0).getId(), equalTo(o4Id));
        Assert.assertThat(result.get(1).getId(), equalTo(o2Id));
        Assert.assertThat(result.get(2).getId(), equalTo(o3Id));
        Assert.assertThat(result.get(3).getId(), equalTo(o1Id));

    }

    @Test
    public void testLimit() throws InterruptedException {
        //构造查询所需的数据
        for (int i = 0; i < 1100; i++) {
            BaasObject object = new BaasObject();
            object.put("string", "object1");
            object.put("number", i);
            object.put("boolean", true);
            objectService.insert(app.getId(), "cloud", "ObjectTest", object, null, false);
            Thread.sleep(1);
        }
        //限制为100个
        List<BaasObject> result = objectService.list(app.getId(), "admin", "ObjectTest", null, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(100));

        //限制为500个
        result = objectService.list(app.getId(), "admin", "ObjectTest", null, null, null, 500, 0, null, false);
        Assert.assertThat(result.size(), equalTo(500));
        Assert.assertThat(result.get(0).get("number"), equalTo(1099));
        Assert.assertThat(result.get(499).get("number"), equalTo(600));

        //限制为1100个 实际为1000
        result = objectService.list(app.getId(), "admin", "ObjectTest", null, null, null, 1100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1000));
        Assert.assertThat(result.get(0).get("number"), equalTo(1099));
        Assert.assertThat(result.get(999).get("number"), equalTo(100));

        //测试skip 获取100-200
        result = objectService.list(app.getId(), "admin", "ObjectTest", null, null, null, 100, 100, null, false);
        Assert.assertThat(result.size(), equalTo(100));
        Assert.assertThat(result.get(0).get("number"), equalTo(999));
        Assert.assertThat(result.get(99).get("number"), equalTo(900));

        //测试skip 获取1000-1100
        result = objectService.list(app.getId(), "admin", "ObjectTest", null, null, null, 100, 1000, null, false);
        Assert.assertThat(result.size(), equalTo(100));
        Assert.assertThat(result.get(0).get("number"), equalTo(99));
        Assert.assertThat(result.get(99).get("number"), equalTo(0));

    }

    /**
     * 测试包含字段
     */
    /* 包含关系结构如下
       a
      | |
      b c
       | |
       d e
       |
       f
         */
    @Test
    public void testInclude() {
        //构造环境
        BaasObject e = new BaasObject();
        e.put("name", "e");
        String idE = objectService.insert(app.getId(), "cloud", "E", e, null, false).getId();

        BaasObject f = new BaasObject();
        f.put("name", "f");
        String idF = objectService.insert(app.getId(), "cloud", "F", f, null, false).getId();

        BaasObject d = new BaasObject();
        d.put("name", "d");
        String idD = objectService.insert(app.getId(), "cloud", "D", d, null, false).getId();

        BaasObject pointF = new BaasObject();
        pointF.put("__type", "Pointer");
        pointF.put("className", "F");
        pointF.put("_id", idF);
        d.put("f", pointF);
        objectService.update(app.getId(), "admin", "D", idD, d, null, false);

        BaasObject c = new BaasObject();
        c.put("name", "c");
        String idC = objectService.insert(app.getId(), "cloud", "C", c, null, false).getId();

        BaasObject pointD = new BaasObject();
        pointD.put("__type", "Pointer");
        pointD.put("className", "D");
        pointD.put("_id", idD);
        c.put("d", pointD);

        BaasObject pointE = new BaasObject();
        pointE.put("__type", "Pointer");
        pointE.put("className", "E");
        pointE.put("_id", idE);
        c.put("e", pointE);
        objectService.update(app.getId(), "admin", "C", idC, c, null, false);

        BaasObject a = new BaasObject();
        a.put("name", "a");
        String idA = objectService.insert(app.getId(), "cloud", "A", a, null, false).getId();

        BaasObject pointC = new BaasObject();
        pointC.put("__type", "Pointer");
        pointC.put("className", "C");
        pointC.put("_id", idC);
        a.put("c", pointC);

        BaasObject b = new BaasObject();
        b.put("name", "b");
        String idB = objectService.insert(app.getId(), "cloud", "B", b, null, false).getId();

        BaasObject pointB = new BaasObject();
        pointB.put("__type", "Pointer");
        pointB.put("className", "B");
        pointB.put("_id", idB);
        a.put("b", pointB);

        objectService.update(app.getId(), "admin", "A", idA, a, null, false);

        //查询验证
        //a包含b不包含c
        BaasInclude include = objectService.getBaasInclude("b");
        List<BaasObject> result = objectService.list(app.getId(), "admin", "A", null, null, include, 1, 0, null, false);
        BaasObject resultA = result.get(0);
        BaasObject bina = (BaasObject) resultA.get("b");
        BaasObject cina = (BaasObject) resultA.get("c");
        Assert.assertThat(bina.getId(), equalTo(idB));
        Assert.assertThat(bina.get("name"), equalTo("b"));
        Assert.assertThat(cina.getId(), equalTo(idC));
        Assert.assertThat(cina.get("name"), equalTo(null));

        //a包含b包含c
        include = objectService.getBaasInclude("b,c");
        result = objectService.list(app.getId(), "admin", "A", null, null, include, 1, 0, null, false);
        resultA = result.get(0);
        bina = (BaasObject) resultA.get("b");
        cina = (BaasObject) resultA.get("c");
        BaasObject dinc = (BaasObject) cina.get("d");
        BaasObject einc = (BaasObject) cina.get("e");
        Assert.assertThat(bina.getId(), equalTo(idB));
        Assert.assertThat(bina.get("name"), equalTo("b"));
        Assert.assertThat(cina.getId(), equalTo(idC));
        Assert.assertThat(cina.get("name"), equalTo("c"));
        Assert.assertThat(dinc.getId(), equalTo(idD));
        Assert.assertThat(dinc.get("name"), equalTo(null));
        Assert.assertThat(einc.getId(), equalTo(idE));
        Assert.assertThat(einc.get("name"), equalTo(null));

        //a包含b包含c c包含d包含e
        include = objectService.getBaasInclude("b,c,c.d,c.e");
        result = objectService.list(app.getId(), "admin", "A", null, null, include, 1, 0, null, false);
        resultA = result.get(0);
        cina = (BaasObject) resultA.get("c");
        dinc = (BaasObject) cina.get("d");
        einc = (BaasObject) cina.get("e");
        Assert.assertThat(dinc.getId(), equalTo(idD));
        Assert.assertThat(dinc.get("name"), equalTo("d"));
        Assert.assertThat(einc.getId(), equalTo(idE));
        Assert.assertThat(einc.get("name"), equalTo("e"));

        //a包含c c包含d d包含f
        include = objectService.getBaasInclude("c.d.f");
        result = objectService.list(app.getId(), "admin", "A", null, null, include, 1, 0, null, false);
        resultA = result.get(0);
        cina = (BaasObject) resultA.get("c");
        dinc = (BaasObject) cina.get("d");
        BaasObject find = (BaasObject) dinc.get("f");
        Assert.assertThat(dinc.getId(), equalTo(idD));
        Assert.assertThat(dinc.get("name"), equalTo("d"));
        Assert.assertThat(find.getId(), equalTo(idF));
        Assert.assertThat(find.get("name"), equalTo("f"));

    }

    @Test
    public void testRequiredField() {
        //测试非空字段
        //创建测试用类
        Clazz clazz = new Clazz();
        clazz.setName("RequiredTest");
        clazzService.insert(app.getId(), clazz);
        Field fieldString = new Field(FieldType.STRING, "string");
        fieldString.setRequired(true);
        fieldService.insert(app.getId(), "RequiredTest", fieldString);

        BaasObject t = new BaasObject();

        try {
            objectService.insert(app.getId(), "admin", "RequiredTest", t, null, false);
            Assert.fail();
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.OBJECT_FIELD_REQUIRED.getCode()));
        }

        t.put("string", "string");
        String id = objectService.insert(app.getId(), "admin", "RequiredTest", t, null, false).getId();

        t = objectService.get(app.getId(), "admin", "RequiredTest", id);
        //检查字段是否存储正确
        Assert.assertThat(t.get("string"), equalTo("string"));
    }

    @Test
    public void testSecurityField() {
        //测试安全字段
        //创建测试用类
        Clazz clazz = new Clazz();
        clazz.setName("SecurityTest");
        clazzService.insert(app.getId(), clazz);
        Field fieldString = new Field(FieldType.STRING, "string");
        fieldString.setSecurity(true);
        fieldService.insert(app.getId(), "SecurityTest", fieldString);

        BaasObject t = new BaasObject();
        t.put("string", "string");
        String objectId = objectService.insert(app.getId(), "admin", "SecurityTest", t, null, true).getId();

        //普通用户权限查询安全字段不显示
        BaasObject object = objectService.get(app.getId(), "admin", "SecurityTest", objectId, null, null, false);
        Assert.assertThat(object.get("string"), nullValue());

        object = objectService.list(app.getId(), "admin", "SecurityTest", null, null, null, 100, 0, null, false).get(0);
        Assert.assertThat(object.get("string"), nullValue());

        //管理权限可以查询安全字段
        object = objectService.get(app.getId(), "admin", "SecurityTest", objectId, null, null, true);
        Assert.assertThat(object.get("string"), equalTo("string"));

        object = objectService.list(app.getId(), "admin", "SecurityTest", null, null, null, 100, 0, null, true).get(0);
        //普通用户权限查询安全字段不显示
        Assert.assertThat(object.get("string"), equalTo("string"));

    }

    /**
     * 测试子查询
     */
    @Test
    public void testSubFind() {
        //构造环境
        /* 包含关系结构如下
          a b
           |
           c
           |
           d
         */
        BaasObject c = new BaasObject();
        c.put("name", "c");
        String idC = objectService.insert(app.getId(), "cloud", "C", c, null, false).getId();

        BaasObject c2 = new BaasObject();
        c2.put("name", "c");

        BaasObject d = new BaasObject();
        d.put("name", "d");
        String idD = objectService.insert(app.getId(), "cloud", "D", d, null, false).getId();

        BaasObject a = new BaasObject();
        a.put("name", "a");
        String idA = objectService.insert(app.getId(), "cloud", "A", a, null, false).getId();

        BaasObject a2 = new BaasObject();
        a2.put("name", "a");
        String idA2 = objectService.insert(app.getId(), "cloud", "A", a2, null, false).getId();

        BaasObject b = new BaasObject();
        b.put("name", "b");
        String idB = objectService.insert(app.getId(), "cloud", "B", b, null, false).getId();

        BaasObject pointC = new BaasObject();
        pointC.put("__type", "Pointer");
        pointC.put("className", "C");
        pointC.put("_id", idC);
        a.put("c", pointC);
        a2.put("c", pointC);
        b.put("c", pointC);

        objectService.update(app.getId(), "admin", "A", idA, a, null, false);
        objectService.update(app.getId(), "admin", "A", idA2, a2, null, false);
        objectService.update(app.getId(), "admin", "B", idB, b, null, false);

        BaasObject pointD = new BaasObject();
        pointD.put("__type", "Pointer");
        pointD.put("className", "D");
        pointD.put("_id", idD);
        c.put("d", pointD);

        objectService.update(app.getId(), "admin", "C", idC, c, null, false);

        //查询A类中c字段中name为c的所有对象
        BaasQuery query = new BaasQuery();
        BaasObject queryC = new BaasObject();
        query.put("c", queryC);
        BaasObject subQuery = new BaasObject();
        queryC.put("$sub", subQuery);
        BaasObject where = new BaasObject();
        where.put("name", "c");
        subQuery.put("where", where);
        subQuery.put("searchClass", "C");

        List<BaasObject> result = objectService.list(app.getId(), "admin", "A", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(2));
        a = result.get(0);
        Assert.assertThat(a.getString("name"), equalTo("a"));

        result = objectService.list(app.getId(), "admin", "B", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(1));
        b = result.get(0);
        Assert.assertThat(b.getString("name"), equalTo("b"));

        //查询A类中c字段 匹配于B类中name字段为b的c对象
        query = new BaasQuery();
        BaasObject queryB = new BaasObject();
        query.put("c", queryB);
        subQuery = new BaasObject();
        queryB.put("$sub", subQuery);
        where = new BaasObject();
        where.put("name", "b");
        subQuery.put("where", where);
        subQuery.put("searchClass", "B");
        subQuery.put("targetClass", "C");
        subQuery.put("searchKey", "c");

        result = objectService.list(app.getId(), "admin", "A", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(2));
        a = result.get(0);
        Assert.assertThat(a.getString("name"), equalTo("a"));

        //查询A类中 c字段中的 d字段中的name为d的对象
        query = jsonUtil.readValue("{\"c\":{\"$sub\":{\"where\":{\"d\":{\"$sub\":{\"where\":{\"name\":\"d\"},\"searchClass\":\"D\"}}},\"searchClass\":\"C\"}}}", BaasQuery.class);
        result = objectService.list(app.getId(), "admin", "A", query, null, null, 100, 0, null, false);
        Assert.assertThat(result.size(), equalTo(2));
        a = result.get(0);
        Assert.assertThat(a.getString("name"), equalTo("a"));

    }

    @Test
    public void testCount() {
        //构造查询所需的数据
        for (int i = 0; i < 100; i++) {
            BaasObject object = new BaasObject();
            object.put("string", "object1");
            object.put("number", i);
            object.put("boolean", true);
            objectService.insert(app.getId(), "cloud", "ObjectTest", object, null, false);
        }
        long count = objectService.count(app.getId(), "ObjectTest", null, null, false);
        Assert.assertThat(count, equalTo(100L));

        BaasQuery query = new BaasQuery();
        BaasObject number = new BaasObject();
        number.put("$gte", 50);
        query.put("number", number);
        count = objectService.count(app.getId(), "ObjectTest", query, null, false);
        Assert.assertThat(count, equalTo(50L));
    }

}
