package com.javabaas.server.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.common.sign.AuthChecker;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.user.entity.*;
import com.javabaas.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * Created by Staryet on 15/8/13.
 */
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthChecker authChecker;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private JSONUtil jsonUtil;

    /**
     * 注册用户
     *
     * @param body 用户信息
     * @return 注册结果及用户id
     * @throws SimpleError
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult register(@RequestHeader(value = "JB-AppId") String appId,
                                 @RequestHeader(value = "JB-Plat") String plat,
                                 @RequestBody String body) {
        BaasUser register = jsonUtil.readBaas(body, BaasUser.class);
        BaasUser user = userService.register(appId, plat, register);
        SimpleResult result = SimpleResult.success();
        BaasObject object = new BaasObject();
        object.setId(user.getId());
        object.put("createdAt", user.getCreatedAt());
        object.put("sessionToken", user.getSessionToken());
        result.putData("result", object);
        return result;
    }

    /**
     * 绑定社交平台
     *
     * @param appId    应用id
     * @param authData 社交平台信息
     * @param id       用户id
     * @param platform 社交平台名称 对应BaasSnsType.getCode
     * @return 请求结果
     * @throws SimpleError
     */
    @RequestMapping(value = "/{id}/binding/{platform}", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult bindingSns(@RequestHeader(value = "JB-AppId") String appId,
                                   @RequestHeader(value = "JB-Plat") String plat,
                                   @RequestBody String authData,
                                   @PathVariable String id,
                                   @PathVariable int platform) {
        BaasSnsType baasSnsType = BaasSnsType.getType(platform);
        if (baasSnsType == null) {
            throw new SimpleError(SimpleCode.USER_AUTH_PLATFORM_MISSING);
        }
        //处理权限
        boolean isMaster = authChecker.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        BaasAuth auth = jsonUtil.readBaas(authData, BaasAuth.class);
        userService.bindingSns(appId, plat, id, baasSnsType, auth, currentUser, isMaster);
        return SimpleResult.success();
    }

    /**
     * 解绑社交平台
     *
     * @param appId    应用id
     * @param id       用户id
     * @param platform 社交平台名称
     * @return 请求结果
     * @throws SimpleError
     */
    @RequestMapping(value = "/{id}/release/{platform}", method = RequestMethod.DELETE)
    @ResponseBody
    public SimpleResult releaseSns(@RequestHeader(value = "JB-AppId") String appId,
                                   @RequestHeader(value = "JB-Plat") String plat,
                                   @PathVariable String id,
                                   @PathVariable String platform) {
        //处理权限
        boolean isMaster = authChecker.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        userService.releaseSns(appId, plat, id, platform, currentUser, isMaster);
        return SimpleResult.success();
    }

    /**
     * 更新用户信息
     *
     * @param body 用户信息
     * @return 请求结果
     * @throws SimpleError
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult update(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @RequestBody String body,
                               @PathVariable String id) {
        BaasUser user = jsonUtil.readBaas(body, BaasUser.class);
        //处理权限
        boolean isMaster = authChecker.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        userService.update(appId, plat, id, user, currentUser, isMaster);
        return SimpleResult.success();
    }

    @RequestMapping(value = "/{id}/updatePassword", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult updatePassword(@RequestHeader(value = "JB-AppId") String appId,
                                       @RequestHeader(value = "JB-Plat") String plat,
                                       @RequestBody String body,
                                       @PathVariable String id) {
        Map<String, String> data = jsonUtil.readValue(body, new TypeReference<HashMap<String, String>>() {
        });
        String oldPassword = data.get("oldPassword");
        String newPassword = data.get("newPassword");
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        BaasUser user = userService.updatePassword(appId, plat, id, oldPassword, newPassword, currentUser);
        SimpleResult result = SimpleResult.success();
        result.putData("result", new BaasObject("sessionToken", user.getSessionToken()));
        return result;
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 请求结果
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult login(@RequestHeader(value = "JB-AppId") String appId,
                              @RequestHeader(value = "JB-Plat") String plat,
                              @RequestParam(required = true) String username,
                              @RequestParam(required = true) String password) {
        BaasUser user = userService.login(appId, plat, username, password);
        SimpleResult result = SimpleResult.success();
        result.putData("result", user);
        return result;
    }

    /**
     * 使用社交平台数据登录
     *
     * @param authData 社交平台数据
     * @param platform 平台类型
     */
    @RequestMapping(value = "/loginWithSns/{platform}", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult loginWithSns(@RequestHeader(value = "JB-AppId") String appId,
                                     @RequestHeader(value = "JB-Plat") String plat,
                                     @RequestBody String authData,
                                     @PathVariable int platform) {
        BaasSnsType baasSnsType = BaasSnsType.getType(platform);
        if (baasSnsType == null) {
            throw new SimpleError(SimpleCode.USER_AUTH_PLATFORM_MISSING);
        }
        BaasAuth auth = jsonUtil.readBaas(authData, BaasAuth.class);
        BaasUser user = userService.registerWithSns(appId, plat, baasSnsType, auth, null);
        SimpleResult result = SimpleResult.success();
        result.putData("result", user);
        return result;
    }

    /**
     * 使用社交平台信息登录(用户不存在时自动注册)
     *
     * @param register 社交平台及用户数据
     * @param platform 平台类型
     */
    @RequestMapping(value = "/registerWithSns/{platform}", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult registerWithSns(@RequestHeader(value = "JB-AppId") String appId,
                                        @RequestHeader(value = "JB-Plat") String plat,
                                        @Valid @RequestBody BaasSnsRegister register,
                                        @PathVariable int platform) {
        BaasSnsType baasSnsType = BaasSnsType.getType(platform);
        if (baasSnsType == null) {
            throw new SimpleError(SimpleCode.USER_AUTH_PLATFORM_MISSING);
        }
        BaasUser user = userService.registerWithSns(appId, plat, baasSnsType, register.getAuth(), register.getUser());
        SimpleResult result = SimpleResult.success();
        result.putData("result", user);
        return result;
    }

    /**
     * 手机验证码注册登录
     */
    @RequestMapping(value = "/loginWithPhone", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult loginWithPhone(@RequestHeader(value = "JB-AppId") String appId,
                                       @RequestHeader(value = "JB-Plat") String plat,
                                       @Valid @RequestBody BaasPhoneRegister register) {
        BaasUser user = userService.loginWithPhone(appId, plat, register);
        SimpleResult result = SimpleResult.success();
        result.putData("result", user);
        return result;
    }

    /**
     * 获取注册登录短信验证码
     *
     * @param phone 手机号码
     * @return 请求结果
     */
    @RequestMapping(value = "/getSmsCode/{phone}", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult getSmsCode(@RequestHeader(value = "JB-AppId") String appId,
                                   @RequestHeader(value = "JB-Plat") String plat,
                                   @PathVariable String phone) {
        return userService.getRegisterSmsCode(appId, plat, phone);
    }

    /**
     * 绑定手机号
     *
     * @return 请求结果
     */
    @RequestMapping(value = "/bindPhone", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult bindPhone(@RequestHeader(value = "JB-AppId") String appId,
                                  @RequestHeader(value = "JB-Plat") String plat,
                                  @Valid @RequestBody BaasPhoneRegister register) {
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        return userService.bindPhone(appId, plat, currentUser, register);
    }

    /**
     * 获取绑定用短信验证码
     *
     * @param phone 手机号码
     * @return 请求结果
     */
    @RequestMapping(value = "/getBindCode/{phone}", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult getBindCode(@RequestHeader(value = "JB-AppId") String appId,
                                    @RequestHeader(value = "JB-Plat") String plat,
                                    @PathVariable String phone) {
        return userService.getBindSmsCode(appId, plat, phone);
    }

    /**
     * 重置sessionToken
     *
     * @param appId 应用id
     * @param plat  平台
     * @param id    用户id
     * @return 结果
     */
    @RequestMapping(value = "/{id}/resetSessionToken", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult resetSessionToken(@RequestHeader(value = "JB-AppId") String appId,
                                          @RequestHeader(value = "JB-Plat") String plat,
                                          @PathVariable String id) {
        userService.resetSessionToken(appId, plat, id);
        return SimpleResult.success();
    }

}
