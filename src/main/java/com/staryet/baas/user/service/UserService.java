package com.staryet.baas.user.service;

import com.staryet.baas.common.entity.SimpleCode;
import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.common.util.JSONUtil;
import com.staryet.baas.object.entity.BaasObject;
import com.staryet.baas.object.entity.BaasQuery;
import com.staryet.baas.object.service.ObjectService;
import com.staryet.baas.user.entity.BaasAuth;
import com.staryet.baas.user.entity.BaasUser;
import com.staryet.baas.user.util.SnsAuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 用户服务
 * Created by Staryet on 15/8/13.
 */
@Service
public class UserService {

    public static String USER_CLASS_NAME = "_User";
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private JSONUtil jsonUtil;
    @Autowired
    private SnsAuthUtil authUtil;

    public BaasUser register(String appId, String plat, BaasUser user) {
        String username = user.getUsername();
        String password = user.getPassword();
        if (StringUtils.isEmpty(password)) {
            //密码禁止为空
            throw new SimpleError(SimpleCode.USER_EMPTY_PASSWORD);
        }
        if (StringUtils.isEmpty(username)) {
            //用户名禁止为空
            throw new SimpleError(SimpleCode.USER_EMPTY_USERNAME);
        }
        if (!isNameValid(username)) {
            //用户名不合法
            throw new SimpleError(SimpleCode.USER_INVALID_USERNAME);
        }
        BaasUser exist = get(appId, plat, username, null, true);
        if (exist != null) {
            //用户已存在
            throw new SimpleError(SimpleCode.USER_ALREADY_EXIST);
        }
        user.setPassword(encrypt(username, password));
        user.setSessionToken(getSessionToken());
        //禁止设置ACL字段
        user.remove("acl");
        BaasObject object = objectService.insert(appId, plat, USER_CLASS_NAME, user, null, true);
        return new BaasUser(object);
    }

    /**
     * 获取用户
     *
     * @param username 用户名
     * @return 用户
     * @throws SimpleError
     */
    public BaasUser get(String appId, String plat, String username, BaasUser currentUser, boolean isMaster) {
        BaasQuery query = new BaasQuery();
        query.put("username", username);
        List<BaasObject> users = objectService.list(appId, plat, USER_CLASS_NAME, query, null, null, 1, 0, currentUser, isMaster);
        if (users.size() == 0) {
            return null;
        }
        return new BaasUser(users.get(0));
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     */
    public BaasUser login(String appId, String plat, String username, String password) {
        if (StringUtils.isEmpty(password)) {
            //密码禁止为空
            throw new SimpleError(SimpleCode.USER_EMPTY_PASSWORD);
        }
        BaasUser user = get(appId, plat, username, null, true);
        if (user == null) {
            throw new SimpleError(SimpleCode.USER_NOT_EXIST);
        }
        String passwordMd5 = encrypt(username, password);
        String passwordExist = user.getPassword();
        if (!passwordMd5.equals(passwordExist)) {
            //密码错误
            throw new SimpleError(SimpleCode.USER_WRONG_PASSWORD);
        }
        user.remove("password");
        return user;
    }

    /**
     * 使用第三方平台登录信息进行登录
     *
     * @param appId    应用id
     * @param platform 名称
     * @param auth     授权信息
     * @return 用户信息
     */
    public BaasUser loginWithSns(String appId, String plat, String platform, BaasAuth auth) {
        //验证授权信息
        if (!authUtil.verifyAuthData(platform, auth)) {
            //授权无效
            throw new SimpleError(SimpleCode.USER_AUTH_REJECT);
        }
        BaasUser user = getUserByAuth(appId, plat, platform, auth.getUid());
        if (user == null) {
            throw new SimpleError(SimpleCode.USER_NOT_EXIST);
        }
        //更新accessToken
        BaasObject authNow = user.getAuth();
        //填充授权信息
        authNow.put(platform, auth);
        BaasUser userNew = new BaasUser();
        userNew.setAuth(authNow);
        objectService.update(appId, plat, UserService.USER_CLASS_NAME, user.getId(), userNew, null, true);
        //返回用户信息
        user.setPassword("");
        return user;
    }

    public void update(String appId, String plat, String id, BaasUser user, BaasUser currentUser, boolean isMaster) {
        if (!isMaster) {
            //非管理权限
            if (currentUser == null || !currentUser.getId().equals(id)) {
                //非本人禁止修改用户信息
                throw new SimpleError(SimpleCode.USER_NOT_MATCH);
            }
            //非管理权限,禁止修改敏感信息,敏感信息使用特定接口修改
            user.remove("phone");
            user.remove("email");
            user.remove("auth");
        }
        String newPassword = user.getPassword();
        if (!StringUtils.isEmpty(newPassword)) {
            //密码字段不为空
            String newPasswordEncrypt = encrypt(currentUser.getUsername(), user.getPassword());
            if (!newPasswordEncrypt.equals(currentUser.getPassword())) {
                //密码字段被修改 更新SessionToken
                user.setPassword(newPasswordEncrypt);
                String sessionTokenNow = currentUser.getSessionToken();
                if (!StringUtils.isEmpty(sessionTokenNow)) {
                    //删除原有用户缓存
                    deleteUserCache(appId, sessionTokenNow);
                }
                user.setSessionToken(getSessionToken());
            }
        } else {
            //清除空的密码字段
            user.remove("password");
        }
        //禁止修改用户名
        user.remove("username");
        //禁止修改ACL字段
        user.remove("acl");
        objectService.update(appId, plat, UserService.USER_CLASS_NAME, id, user, null, true);
        //更新成功 清除用户缓存
        deleteUserCache(appId, user.getSessionToken());
    }

    public BaasUser updatePassword(String appId, String plat, String id, String oldPassword, String newPassword, BaasUser currentUser) {
        if (currentUser == null || !currentUser.getId().equals(id)) {
            //非本人禁止修改用户信息
            throw new SimpleError(SimpleCode.USER_NOT_MATCH);
        }
        if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(newPassword)) {
            throw new SimpleError(SimpleCode.REQUEST_PARAM_ERROR);
            //参数不足
        }
        BaasUser user = get(appId, plat, currentUser.getUsername(), null, true);
        String username = user.getUsername();
        String passwordMd5 = encrypt(username, oldPassword);
        String passwordExist = user.getPassword();
        if (!passwordMd5.equals(passwordExist)) {
            //密码错误
            throw new SimpleError(SimpleCode.USER_WRONG_PASSWORD);
        }
        //修改密码
        user.setPassword(encrypt(username, newPassword));
        //重置SessionToken
        String oldSessionToken = user.getSessionToken();
        user.setSessionToken(getSessionToken());
        objectService.update(appId, plat, UserService.USER_CLASS_NAME, id, user, null, true);
        //更新成功 清除用户缓存
        deleteUserCache(appId, oldSessionToken);
        return user;
    }

    public void bindingSns(String appId, String plat, String id, String platform, BaasAuth auth, BaasUser currentUser, boolean isMaster) {
        if (!isMaster) {
            //非管理权限
            if (currentUser == null || !currentUser.getId().equals(id)) {
                //非本人禁止修改社交平台信息
                throw new SimpleError(SimpleCode.USER_NOT_MATCH);
            }
        }
        //验证授权信息是否有效
        if (!authUtil.verifyAuthData(platform, auth)) {
            //授权无效
            throw new SimpleError(SimpleCode.USER_AUTH_REJECT);
        } else {
            //验证是否已经绑定现有用户
            BaasUser exist = getUserByAuth(appId, plat, platform, auth.getUid());
            if (exist != null) {
                //该第三方用户信息已经被其他用户绑定,禁止重复绑定
                throw new SimpleError(SimpleCode.USER_AUTH_EXIST);
            }
            //授权有效 将授权信息与用户绑定
            BaasObject object = objectService.get(appId, plat, USER_CLASS_NAME, id);
            BaasUser userNow = new BaasUser(object);
            BaasObject authNow = userNow.getAuth();
            if (authNow == null) {
                //当前授权信息为空 创建新的授权信息
                authNow = new BaasObject();
            }
            //填充授权信息
            authNow.put(platform, auth);
            BaasUser userNew = new BaasUser();
            userNew.setAuth(authNow);
            objectService.update(appId, plat, UserService.USER_CLASS_NAME, id, userNew, null, true);
            //更新成功 清除用户缓存
            deleteUserCache(appId, userNow.getSessionToken());
        }
    }

    public void releaseSns(String appId, String plat, String id, String platform, BaasUser currentUser, boolean isMaster) {
        if (!isMaster) {
            //非管理权限
            if (currentUser == null || !currentUser.getId().equals(id)) {
                //非本人禁止修改社交平台信息
                throw new SimpleError(SimpleCode.USER_NOT_MATCH);
            }
        }
        BaasObject object = objectService.get(appId, plat, USER_CLASS_NAME, id);
        BaasUser userNow = new BaasUser(object);
        BaasObject authNow = userNow.getAuth();
        if (authNow == null) {
            //当前授权信息为空 无需解绑 直接返回
            return;
        }
        //清除对应平台的授权信息
        authNow.remove(platform);
        BaasUser userNew = new BaasUser();
        userNew.setAuth(authNow);
        objectService.update(appId, plat, UserService.USER_CLASS_NAME, id, userNew, null, true);
        //更新成功 清除用户缓存
        deleteUserCache(appId, userNow.getSessionToken());
    }

    public String encrypt(String username, String password) {
        return DigestUtils.md5DigestAsHex((username + "_._" + password).getBytes());
    }

    /**
     * 获取当前用户
     *
     * @return 当前用户
     */
    public BaasUser getCurrentUser(String appId, String plat, HttpServletRequest request) {
        String sessionToken = request.getHeader("JB-SessionToken");
        if (sessionToken == null) {
            //未使用sessionToken 为未登录状态
            return null;
        } else {
            //使用sessionToken 为登录状态
            //此处plat为null 为了不进行api统计
            return getUserBySessionToken(appId, null, sessionToken);
        }
    }

    public BaasUser getUserBySessionToken(String appId, String plat, String sessionToken) {
        BaasUser userCache = getUserCache(appId, sessionToken);
        if (userCache == null) {
            //未找到缓存 查询用户数据
            BaasQuery query = new BaasQuery();
            query.put("sessionToken", sessionToken);
            List<BaasObject> users = objectService.list(appId, plat, USER_CLASS_NAME, query, null, null, 1, 0, null, true);
            if (users.size() == 0) {
                //未查询到 sessionToken无效 提示错误
                throw new SimpleError(SimpleCode.USER_SESSION_TOKEN_ERROR);
            }
            BaasUser user = new BaasUser(users.get(0));
            setUserCache(appId, sessionToken, user);
            return user;
        } else {
            return userCache;
        }
    }

    private BaasUser getUserByAuth(String appId, String plat, String platform, String uid) {
        BaasQuery query = new BaasQuery();
        query.put("auth." + platform + ".uid", uid);
        List<BaasObject> users = objectService.list(appId, plat, USER_CLASS_NAME, query, null, null, 1, 0, null, true);
        if (users.size() == 0) {
            return null;
        }
        return new BaasUser(users.get(0));
    }

    public void resetSessionToken(String appId, String plat, String id) {
        BaasObject object = objectService.get(appId, plat, USER_CLASS_NAME, id);
        BaasUser user = new BaasUser(object);
        String sessionToken = user.getSessionToken();
        //更新用户信息
        user.setSessionToken(getSessionToken());
        objectService.update(appId, plat, UserService.USER_CLASS_NAME, id, user, null, true);
        //清除缓存的用户信息
        deleteUserCache(appId, sessionToken);
    }

    private BaasUser getUserCache(String appId, String sessionToken) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String userStr = ops.get("App_" + appId + USER_CLASS_NAME + "_" + sessionToken);
        return jsonUtil.readValue(userStr, BaasUser.class);
    }

    private void setUserCache(String appId, String sessionToken, BaasUser user) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("App_" + appId + USER_CLASS_NAME + "_" + sessionToken, jsonUtil.writeValueAsString(user));
    }

    private void deleteUserCache(String appId, String sessionToken) {
        if (!StringUtils.isEmpty(sessionToken)) {
            redisTemplate.delete("App_" + appId + USER_CLASS_NAME + "_" + sessionToken);
        }
    }

    public void deleteUser(String appId, String plat, String name) {
        BaasQuery query = new BaasQuery();
        query.put("username", name);
        List<BaasObject> users = objectService.list(appId, plat, USER_CLASS_NAME, query, null, null, 1, 0, null, true);
        if (users.size() > 0) {
            String sessionToken = users.get(0).getString("sessionToken");
            deleteUserCache(appId, sessionToken);
            objectService.delete(appId, plat, USER_CLASS_NAME, users.get(0).getString("id"), null, true);
        }
    }

    private String getSessionToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 检查用户名
     *
     * @param name 用户名
     * @return 是否满足规则
     */
    public boolean isNameValid(String name) {
        String regex = "^[a-zA-Z0-9_@.]*$";
        return Pattern.matches(regex, name);
    }

}
