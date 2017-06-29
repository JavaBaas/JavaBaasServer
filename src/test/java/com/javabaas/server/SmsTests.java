package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.sms.entity.SmsSendResult;
import com.javabaas.server.sms.entity.SmsSendResultCode;
import com.javabaas.server.sms.handler.impl.MockSmsHandler;
import com.javabaas.server.sms.service.SmsService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

/**
 * 测试推送
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SmsTests {

    @Autowired
    private AppService appService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private MockSmsHandler mockSmsHandler;

    private final String PHONE_NUMBER = "13800138000";

    private App app;

    @Before
    public void before() {
        smsService.setSmsHandler(mockSmsHandler);
        appService.deleteByAppName("SmsTestApp");
        app = new App();
        app.setName("SmsTestApp");
        appService.insert(app);
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testSendSms() {
        Map<String, String> params = new HashMap<>();
        params.put("test", "test");
        smsService.sendSms(PHONE_NUMBER, "sign", "1", params);
        String sms = mockSmsHandler.getSms(PHONE_NUMBER);
        Assert.assertThat(sms, equalTo("test"));
    }

    /**
     * 测试短信验证码逻辑
     */
    @Test
    public void testSendSmsCode() {
        SmsSendResult result = smsService.sendSmsCode(app.getId(), PHONE_NUMBER, 10);
        Assert.assertThat(result.getCode(), equalTo(SmsSendResultCode.SUCCESS.getCode()));
        String code = mockSmsHandler.getSms(PHONE_NUMBER);
        //验证短信验证码正确
        boolean verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, code);
        Assert.assertThat(verifyResult, equalTo(true));
        //短信验证码禁止重复使用
        verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, code);
        Assert.assertThat(verifyResult, equalTo(false));
    }

    /**
     * 测试短信验证码超时失效
     */
    @Test
    public void testSendSmsCodeTimeout() {
        //验证码超时时间为1秒
        SmsSendResult result = smsService.sendSmsCode(app.getId(), PHONE_NUMBER, 1);
        Assert.assertThat(result.getCode(), equalTo(SmsSendResultCode.SUCCESS.getCode()));
        String code = mockSmsHandler.getSms(PHONE_NUMBER);
        //1100毫秒后验证码失效
        try {
            Thread.sleep(1100);
        } catch (InterruptedException ignored) {
        }
        boolean verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, code);
        Assert.assertThat(verifyResult, equalTo(false));
    }

    /**
     * 测试短信验证码错误验证次数
     */
    @Test
    public void testSendSmsCodeTryTimes() {
        //短信验证码错误验证 第5次有效
        SmsSendResult result = smsService.sendSmsCode(app.getId(), PHONE_NUMBER, 10);
        Assert.assertThat(result.getCode(), equalTo(SmsSendResultCode.SUCCESS.getCode()));
        String code = mockSmsHandler.getSms(PHONE_NUMBER);
        for (int i = 0; i < 5; i++) {
            boolean verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, "xxxx");
            Assert.assertThat(verifyResult, equalTo(false));
        }
        boolean verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, code);
        Assert.assertThat(verifyResult, equalTo(true));

        //短信验证码错误验证 5次后失效
        result = smsService.sendSmsCode(app.getId(), PHONE_NUMBER, 10);
        Assert.assertThat(result.getCode(), equalTo(SmsSendResultCode.SUCCESS.getCode()));
        code = mockSmsHandler.getSms(PHONE_NUMBER);
        for (int i = 0; i < 6; i++) {
            verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, "xxxx");
            Assert.assertThat(verifyResult, equalTo(false));
        }
        verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, code);
        Assert.assertThat(verifyResult, equalTo(false));
    }

}
