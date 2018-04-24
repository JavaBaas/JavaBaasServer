package com.javabaas.server.role.service;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.object.entity.BaasAcl;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.role.entity.BaasRole;
import com.javabaas.server.user.entity.BaasUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by zangyilin on 2018/4/16.
 */
@Service
public class RoleService {
    public static String ROLE_CLASS_NAME = "_Role";
    @Autowired
    private ObjectService objectService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private JSONUtil jsonUtil;

    /**
     *  新增角色
     *
     * @param role 角色信息
     * @return 角色
     */
    public BaasRole insert(String appId, String plat, BaasRole role, BaasUser currentUser, boolean isMaster) {
        String name = role.getName();
        BaasAcl acl = role.getAcl();
        if (StringUtils.isEmpty(name)) {
            // 角色名不能为空
            throw new SimpleError(SimpleCode.ROLE_EMPTY_NAME);
        }
        if (!isNameValid(name)) {
            // 角色名不合法
            throw new SimpleError(SimpleCode.ROLE_INVALID_NAME);
        }
        if (acl == null) {
            // 角色acl不能为空
            throw new SimpleError(SimpleCode.ROLE_EMPTY_ACL);
        }
        BaasRole exist = get(appId, plat, name, null, true);
        if (exist != null) {
            // 角色已经存在
            throw new SimpleError(SimpleCode.ROLE_ALREADY_EXIST);
        }

        BaasObject object = objectService.insert(appId, plat, ROLE_CLASS_NAME, role, true, null, true);
        return new BaasRole(object);
    }

    /**
     *  根据角色名称获取角色
     *
     * @param name 角色名
     * @return 角色信息
     */
    public BaasRole get(String appId, String plat, String name, BaasUser currentUser, boolean isMaster) {
        BaasQuery query = new BaasQuery();
        query.put("name", name);
        List<BaasObject> roles = objectService.find(appId, plat, ROLE_CLASS_NAME, query, null, null, null, 1, 0, currentUser, isMaster);
        if (roles.size() == 0) {
            return null;
        }
        return new BaasRole(roles.get(0));
    }

    public BaasRole getRoleByIdWithCache(String appId, String plat, String id) {
        BaasRole cacheRole = getRoleCache(appId, id);
        if (cacheRole == null) {
            // 未查询到缓存 从数据中查询
            BaasQuery query = new BaasQuery();
            BaasObject object = objectService.get(appId, plat, ROLE_CLASS_NAME, id, null, null, null, true);
            if (object == null) {
                return null;
            } else {
                BaasRole role = new BaasRole(object);
                setRoleCache(appId, id, role);
                return role;
            }
        } else {
            return cacheRole;
        }
    }

    public void update(String appId, String plat, String id, BaasRole role, BaasUser currentUser, boolean isMaster) {
        role.remove("name");
        objectService.update(appId, plat, ROLE_CLASS_NAME, id, role, currentUser, isMaster);
        // 更新成功 清楚角色缓存
        deleteRoleCache(appId, id);
    }

    /**
     *  校验角色名称是否合法（由数字、26个英文字母或者下划线组成的字符串）
     *
     * @param name
     * @return
     */
    private boolean isNameValid(String name) {
        String regex = "^\\w+$";
        return Pattern.matches(regex, name);
    }

    private BaasRole getRoleCache(String appId, String id) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String roleStr = ops.get("App_" + appId + ROLE_CLASS_NAME + "_" + id);
        if (StringUtils.isEmpty(roleStr)) {
            return null;
        } else {
            return jsonUtil.readValue(roleStr, BaasRole.class);
        }
    }

    private void deleteRoleCache(String appId, String id) {
        if (!StringUtils.isEmpty(appId) && !StringUtils.isEmpty(id)) {
            redisTemplate.delete("APP_" + appId + ROLE_CLASS_NAME + "_" + id);
        }
    }

    private void setRoleCache(String appId, String id, BaasRole role) {
        if (!StringUtils.isEmpty(appId) && !StringUtils.isEmpty(id)) {
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            ops.set("APP_" + appId + ROLE_CLASS_NAME + "_" + id, jsonUtil.writeValueAsString(role));
        }
    }
}
