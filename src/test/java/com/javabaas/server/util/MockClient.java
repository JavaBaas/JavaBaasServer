package com.javabaas.server.util;

import com.javabaas.server.admin.entity.App;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

/**
 * 测试请求构造器
 * Created by Codi on 2017/7/5.
 */
public class MockClient {

    private MockMvc mockMvc;

    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public MockClient(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    public ResultActions normal(HttpMethod method, String url, Object... params) throws Exception {
        return mockMvc.perform(defaultRequest(method, url, params));
    }

    /**
     * 用户级别请求
     *
     * @param app    应用
     * @param method 请求方法
     * @param url    链接
     * @param params 参数列表
     */
    public ResultActions user(App app, HttpMethod method, String url, String body, Object... params) throws Exception {
        return mockMvc.perform(userRequest(app, method, url, body, params));
    }

    /**
     * 管理权限请求
     *
     * @param app 应用
     * @param url 链接
     */
    public ResultActions master(App app, HttpMethod method, String url, String body, Object... params) throws Exception {
        return mockMvc.perform(masterRequest(app, method, url, body, params));
    }

    /**
     * 超级权限请求
     *
     * @param key adminKey
     * @param url 链接
     */
    public ResultActions admin(String key, HttpMethod method, String url, String body, Object... params) throws Exception {
        return mockMvc.perform(adminRequest(key, method, url, body, params));
    }

    private MockHttpServletRequestBuilder defaultRequest(HttpMethod method, String url, Object... params) throws Exception {
        return MockMvcRequestBuilders.request(method, url, params).header("JB-Plat", "cloud")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder userRequest(App app, HttpMethod method, String url, String body, Object... params) throws
            Exception {
        long timestamp = new Date().getTime();
        String timestampStr = String.valueOf(timestamp);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, url, params).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-Sign", getSign(app.getKey(), timestampStr))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8);
        if (body != null) {
            request.content(body);
        }
        return request;
    }

    private MockHttpServletRequestBuilder masterRequest(App app, HttpMethod method, String url, String body, Object... params) throws
            Exception {
        long timestamp = new Date().getTime();
        String timestampStr = String.valueOf(timestamp);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, url, params).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AppId", app.getId())
                .header("JB-MasterSign", getSign(app.getMasterKey(), timestampStr))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8);
        if (body != null) {
            request.content(body);
        }
        return request;
    }

    private MockHttpServletRequestBuilder adminRequest(String key, HttpMethod method, String url, String body, Object... params) throws
            Exception {
        long timestamp = new Date().getTime();
        String timestampStr = String.valueOf(timestamp);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, url, params).header("JB-Plat", "cloud")
                .header("JB-Timestamp", timestampStr)
                .header("JB-AdminSign", getSign(key, timestampStr))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8);
        if (body != null) {
            request.content(body);
        }
        return request;
    }


    private String getSign(String key, String timestamp) {
        return DigestUtils.md5DigestAsHex((key + ":" + timestamp).getBytes());
    }

}
