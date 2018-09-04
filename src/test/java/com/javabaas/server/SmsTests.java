package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.config.entity.AppConfigEnum;
import com.javabaas.server.config.service.AppConfigService;
import com.javabaas.server.object.entity.BaasObject;
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
    @Autowired
    private AppConfigService appConfigService;

    private final String PHONE_NUMBER = "13800138000";

    private App app;

    @Before
    public void before() {
        appService.deleteByAppName("SmsTestApp");
        app = new App();
        app.setName("SmsTestApp");
        appService.insert(app);
        //配置短信发送器
        appConfigService.setConfig(app.getId(), AppConfigEnum.SMS_HANDLER, "mock");
        appConfigService.setConfig(app.getId(), AppConfigEnum.SMS_SIGN_NAME, "JavaBaas");
        appConfigService.setConfig(app.getId(), AppConfigEnum.SMS_CODE_TEMPLATE_ID, "JavaBaas");
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testSendSms() {
        BaasObject params = new BaasObject();
        params.put("test", "test");
        smsService.sendSms(app.getId(), "admin", PHONE_NUMBER, "1", params);
        String sms = mockSmsHandler.getSms(PHONE_NUMBER);
        Assert.assertThat(sms, equalTo("test"));
    }

    /**
     * 测试短信验证码逻辑
     */
    @Test
    public void testSendSmsCode() {
        SimpleResult result = smsService.sendSmsCode(app.getId(), "admin", PHONE_NUMBER, 10, null);
        Assert.assertThat(result.getCode(), equalTo(SimpleCode.SUCCESS.getCode()));
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
        SimpleResult result = smsService.sendSmsCode(app.getId(), "admin", PHONE_NUMBER, 1, null);
        Assert.assertThat(result.getCode(), equalTo(SimpleCode.SUCCESS.getCode()));
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
    public void testSendSmsCodeTryTimesSuccess() {
        //短信验证码错误验证 第5次有效
        SimpleResult result = smsService.sendSmsCode(app.getId(), "admin", PHONE_NUMBER, 10, null);
        Assert.assertThat(result.getCode(), equalTo(SimpleCode.SUCCESS.getCode()));
        String code = mockSmsHandler.getSms(PHONE_NUMBER);
        for (int i = 0; i < 5; i++) {
            boolean verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, "xxxx");
            Assert.assertThat(verifyResult, equalTo(false));
        }
        boolean verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, code);
        Assert.assertThat(verifyResult, equalTo(true));
    }

    @Test
    public void testSmsCodeTryTimesFail() {
        //短信验证码错误验证 5次后失效
        SimpleResult result = smsService.sendSmsCode(app.getId(), "admin", PHONE_NUMBER, 10, null);
        Assert.assertThat(result.getCode(), equalTo(SimpleCode.SUCCESS.getCode()));
        String code = mockSmsHandler.getSms(PHONE_NUMBER);
        for (int i = 0; i < 5; i++) {
            boolean verifyResult = smsService.verifySmsCode(app.getId(), PHONE_NUMBER, "xxxx");
            Assert.assertThat(verifyResult, equalTo(false));
        }
        try {
            smsService.verifySmsCode(app.getId(), PHONE_NUMBER, code);
        } catch (SimpleError error) {
            Assert.assertThat(error.getCode(), equalTo(SimpleCode.SMS_WRONG_TRY_LIMIT));
        }

    }

}
