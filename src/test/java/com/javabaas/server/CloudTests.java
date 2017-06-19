package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.repository.ClazzRepository;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.cloud.entity.CloudSetting;
import com.javabaas.server.cloud.service.CloudService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Staryet on 15/8/11.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CloudTests {

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
    @Autowired
    private ClazzRepository clazzRepository;
    @LocalServerPort
    private String port;

    private App app;
    private App appNotDeployed;

    @Before
    public void before() {
        appService.deleteByAppName("appNotDeployed");
        //未部署的应用
        appNotDeployed = new App();
        appNotDeployed.setName("appNotDeployed");
        appService.insert(appNotDeployed);
        appNotDeployed = appService.get(appNotDeployed.getId());
        //用于测试的应用
        appService.deleteByAppName("AppCloud");
        app = new App();
        app.setName("AppCloud");
        appService.insert(app);
        app = appService.get(app.getId());
        //部署云代码
        CloudSetting cloudSetting = new CloudSetting();
        cloudSetting.setCustomerHost("http://127.0.0.1:" + port + "/customer/");
        List<String> cloudFunctions = new ArrayList<>();
        cloudFunctions.add("function1");
        cloudFunctions.add("function2");
        cloudFunctions.add("function3");
        cloudFunctions.add("functionNotImpl");
        cloudSetting.setCloudFunctions(cloudFunctions);
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
        appService.delete(appNotDeployed.getId());
    }

    @Test
    public void testNotDeployed() {
        //测试未部署的应用
        try {
            cloudService.cloud(appNotDeployed.getId(), "admin", "NoName", null, false, new HashMap<>());
            Assert.fail();
        } catch (SimpleError e) {
            Assert.assertThat(e.getCode(), equalTo(SimpleCode.CLOUD_NOT_DEPLOYED.getCode()));
        }
        //测试未配置host的情况
        CloudSetting cloudSetting = new CloudSetting();
        List<String> cloudFunctions = new ArrayList<>();
        cloudFunctions.add("function1");
        cloudSetting.setCloudFunctions(cloudFunctions);
        cloudService.deploy(appNotDeployed.getId(), cloudSetting);
        try {
            cloudService.cloud(appNotDeployed.getId(), "admin", "function1", null, false, new HashMap<>());
            Assert.fail();
        } catch (SimpleError e) {
            Assert.assertThat(e.getCode(), equalTo(SimpleCode.CLOUD_NOT_DEPLOYED.getCode()));
        }
    }

    @Test
    public void testFunction() {
        //测试调用错误的方法名称
        try {
            cloudService.cloud(app.getId(), "admin", "NoName", null, false, new HashMap<>());
            Assert.fail();
        } catch (SimpleError e) {
            Assert.assertThat(e.getCode(), equalTo(SimpleCode.CLOUD_FUNCTION_NOT_FOUND.getCode()));
        }
        //测试调用未实现的方法
        try {
            cloudService.cloud(app.getId(), "admin", "functionNotImpl", null, false, new HashMap<>());
            Assert.fail();
        } catch (SimpleError e) {
            Assert.assertThat(e.getCode(), equalTo(SimpleCode.CLOUD_FUNCTION_ERROR.getCode()));
        }
        //测试方法调用
        SimpleResult result = cloudService.cloud(app.getId(), "admin", "function1", null, false, new HashMap<>());
        Assert.assertThat(result.getCode(), equalTo(0));
        Assert.assertThat(result.getMessage(), equalTo("success"));
        //测试用户是否传递成功
        BaasUser user = userService.get(app.getId(), "admin", "user", null, true);
        result = cloudService.cloud(app.getId(), "admin", "function2", user, false, new HashMap<>());
        Assert.assertThat(result.getCode(), equalTo(0));
        Assert.assertThat(result.getMessage(), equalTo(user.getUsername()));
        //测试参数是否传递成功
        Map<String, String> params = new HashMap<>();
        params.put("param1", "param1");
        params.put("param2", "param2");
        result = cloudService.cloud(app.getId(), "admin", "function3", user, false, params);
        Assert.assertThat(result.getCode(), equalTo(1));
        Assert.assertThat(result.getMessage(), equalTo("param1param2"));
    }

}
