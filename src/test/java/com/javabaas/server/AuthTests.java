package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.config.AuthConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Staryet on 15/9/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest("server.port:9000")
public class AuthTests {

    private App app;
    @Autowired
    private AppService appService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private AuthConfig authConfig;

    @Before
    public void before() {
        app = new App();
        app.setName("AuthTestApp");
        appService.insert(app);
        //创建用于测试的类
        Clazz book = new Clazz("Book");
        clazzService.insert(app.getId(), book);

    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testAdmin() {
        //测试无权限时拒绝访问
        testNoAdminAuthUrl("http://127.0.0.1:9000/api/admin/app");
        testNoMasterAuthUrl("http://127.0.0.1:9000/api/master/clazz");
        testNoMasterAuthUrl("http://127.0.0.1:9000/api/master/clazz/Book/field");
        testNoUserAuthUrl("http://127.0.0.1:9000/api/object/Book");
        //测试超级权限
        RestTemplate rest = getAdminRest();
        rest.getForObject("http://127.0.0.1:9000/api/admin/app", String.class);
        //测试管理权限
        rest = getMasterRest(app);
        rest.getForObject("http://127.0.0.1:9000/api/master/clazz/Book", String.class);
        //测试普通权限
        rest = getUserRest(app);
        rest.getForObject("http://127.0.0.1:9000/api/object/Book", String.class);
    }

    /**
     * 缺少超级权限
     *
     * @param url
     */
    private void testNoAdminAuthUrl(String url) {
        RestTemplate rest = getRest();
        try {
            rest.getForObject(url, String.class);
            Assert.fail();
        } catch (HttpClientErrorException exception) {
            String response = exception.getResponseBodyAsString();
            Assert.assertThat(response.contains(String.valueOf(SimpleCode.AUTH_NEED_ADMIN_SIGN.getCode())), equalTo(true));
        }
    }

    /**
     * 缺少管理权限
     *
     * @param url
     */
    private void testNoMasterAuthUrl(String url) {
        RestTemplate rest = getUserRest(app);
        try {
            rest.getForObject(url, String.class);
            Assert.fail();
        } catch (HttpClientErrorException exception) {
            String response = exception.getResponseBodyAsString();
            Assert.assertThat(response.contains(String.valueOf(SimpleCode.AUTH_NEED_MASTER_SIGN.getCode())), equalTo(true));
        }
    }

    /**
     * 缺少普通权限
     *
     * @param url
     */
    private void testNoUserAuthUrl(String url) {
        RestTemplate rest = getRest();
        try {
            rest.getForObject(url, String.class);
            Assert.fail();
        } catch (HttpClientErrorException exception) {
            String response = exception.getResponseBodyAsString();
            Assert.assertThat(response.contains(String.valueOf(SimpleCode.AUTH_LESS.getCode())), equalTo(true));
        }
    }

    public String getSign(String key, String timestamp) {
        return DigestUtils.md5DigestAsHex((key + ":" + timestamp).getBytes());
    }

    public RestTemplate getRest() {
        RestTemplate rest = new RestTemplate();
        ClientHttpRequestInterceptor i = (httpRequest, bytes, clientHttpRequestExecution) -> {
            HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpRequest);
            long timestamp = new Date().getTime();
            requestWrapper.getHeaders().remove("Content-Type");
            requestWrapper.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            requestWrapper.getHeaders().add("JB-Plat", "cloud");
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        rest.setInterceptors(Collections.singletonList(i));
        return rest;
    }

    public RestTemplate getAdminRest() {
        RestTemplate rest = new RestTemplate();
        ClientHttpRequestInterceptor i = (httpRequest, bytes, clientHttpRequestExecution) -> {
            HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpRequest);
            long timestamp = new Date().getTime();
            String timestampStr = String.valueOf(timestamp);
            requestWrapper.getHeaders().remove("Content-Type");
            requestWrapper.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            requestWrapper.getHeaders().add("JB-Timestamp", timestampStr);
            requestWrapper.getHeaders().add("JB-AdminSign", getSign(authConfig.getKey(), timestampStr));
            requestWrapper.getHeaders().add("JB-Plat", "cloud");
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        rest.setInterceptors(Collections.singletonList(i));
        return rest;
    }

    public RestTemplate getMasterRest(App app) {
        RestTemplate rest = new RestTemplate();
        ClientHttpRequestInterceptor i = (httpRequest, bytes, clientHttpRequestExecution) -> {
            HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpRequest);
            long timestamp = new Date().getTime();
            String timestampStr = String.valueOf(timestamp);
            requestWrapper.getHeaders().remove("Content-Type");
            requestWrapper.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            requestWrapper.getHeaders().add("JB-Timestamp", timestampStr);
            requestWrapper.getHeaders().add("JB-AppId", app.getId());
            requestWrapper.getHeaders().add("JB-Plat", "cloud");
            requestWrapper.getHeaders().add("JB-MasterSign", getSign(app.getMasterKey(), timestampStr));
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        rest.setInterceptors(Collections.singletonList(i));
        return rest;
    }

    public RestTemplate getUserRest(App app) {
        RestTemplate rest = new RestTemplate();
        ClientHttpRequestInterceptor i = (httpRequest, bytes, clientHttpRequestExecution) -> {
            HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpRequest);
            long timestamp = new Date().getTime();
            String timestampStr = String.valueOf(timestamp);
            requestWrapper.getHeaders().remove("Content-Type");
            requestWrapper.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            requestWrapper.getHeaders().add("JB-Timestamp", timestampStr);
            requestWrapper.getHeaders().add("JB-AppId", app.getId());
            requestWrapper.getHeaders().add("JB-Plat", "cloud");
            requestWrapper.getHeaders().add("JB-Sign", getSign(app.getKey(), timestampStr));
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        rest.setInterceptors(Collections.singletonList(i));
        return rest;
    }

}
