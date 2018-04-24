package com.javabaas.server.object.service;

import com.javabaas.server.object.entity.BaasAcl;
import com.javabaas.server.object.entity.BaasList;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.role.entity.BaasRole;
import com.javabaas.server.role.service.RoleService;
import com.javabaas.server.user.entity.BaasUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by Codi on 2017/9/12.
 */
@Service
public class AclHandler {

    @Autowired
    private ObjectService objectService;
    @Autowired
    private RoleService roleService;

    /**
     * 添加ACL权限检查
     *
     * @param query    原查询
     * @param user     用户
     * @param isMaster 是否为管理权限
     * @return 合成ACL权限检查以后的查询
     */
    public BaasQuery handleAcl(String appId, BaasQuery query, BaasUser user, boolean isMaster) {
        //处理ACL权限
        if (!isMaster) {
            //非master权限则检查ACL
            BaasAcl aclQuery;
            //全局读权限
            BaasAcl aclAll = new BaasAcl("acl.*.read", true);
            if (user == null) {
                aclQuery = aclAll;
            } else {
                //添加登录用户所拥有的读取权限
                String id = user.getId();
                //用户读权限
                BaasList acls = new BaasList();
                acls.add(aclAll);
                acls.add(new BaasObject("acl." + id + ".read", true));
                // 添加用户所有角色权限
                Set<String> roleNames = getRelatedRoleNames(appId, "cloud", user);
                roleNames.forEach(name -> acls.add(new BaasObject("acl.role:" + name + ".read", true)));
                aclQuery = new BaasAcl("$or", acls);
            }
            //将ACL权限查询合并到主查询
            BaasQuery newQuery = new BaasQuery();
            BaasList list = new BaasList();
            list.add(aclQuery);
            if (query != null) {
                list.add(query);
            }
            newQuery.put("$and", list);
            return newQuery;
        }
        return query;
    }

    /**
     *  检查是否有写权限
     *
     * 检查步骤为：
     * 1、检查当前请求是否有master权限
     * 2、检查acl是否有全员的写权限
     * 3、检查用户是否登录
     * 4、检查acl是否有和角色相关的键值对
     * 5、检查当前用户所有从属的角色是否有修改权限
     *
     * @param acl  acl
     * @param user 当前用户
     * @param isMaster 是否是master权限
     * @return 检查结果
     */
    public boolean checkWriteAccess(String appId, BaasAcl acl, BaasUser user, boolean isMaster) {
        if (isMaster) {
            // master权限有修改权限
            return true;
        }
        if (checkPublicWriteAccess(acl)) {
            // 检查acl是否允许所有人修改
            return true;
        }
        if (user == null || StringUtils.isEmpty(user.getId())) {
            // 检查用户是否登录
            return false;
        } else {
            if (checkWriteAccess(acl, user.getId())) {
                // 检查用户是否有写权限
                return true;
            }
        }

        if (!checkHasRoleAccess(acl)) {
            // 检查是否有角色权限
            return false;
        }
        // 检查用户所属角色以及角色所属的角色是否有修改权限
        Set<String> roleNames = getRelatedRoleNames(appId, "cloud", user);
        boolean flag = false;
        for (String roleName : roleNames) {
            if (checkWriteAccess(acl, "role:" + roleName)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     *  获取登录用户所有角色和角色所属的角色的所有角色名称
     *
     * 处理逻辑为：
     * 1、首先查询出该用户所有的角色的角色名，记录到names(Set)中,
     * 2、将查询出所有角色的id汇总，
     * 3、查询所有roles字段包含步骤2中的汇总id的角色，将所有角色名称记录到names中，同时汇总本次查询的所有角色id（需要考虑去重之前已经查询的角色id）
     * 4、重复步骤3直到不在有新的汇总的角色id，
     * 5、返回记录的names
     *
     * @param user 登录用户
     * @return 角色名称集合
     */
    private Set<String> getRelatedRoleNames(String appId, String plat, BaasUser user) {
        BaasQuery query = new BaasQuery();
        query.put("users", user.getId());
        List<BaasObject> list = objectService.find(appId, plat, RoleService.ROLE_CLASS_NAME, query, null, null, null, 1000, 0, null, true);
        Set<String> names = Collections.synchronizedSet(new HashSet<>());
        Set<String> roleIds = Collections.synchronizedSet(new HashSet<>());
        Set<String> repeatRoleIds = Collections.synchronizedSet(new HashSet<>());
        list.parallelStream().forEach(object -> {
            BaasRole role = new BaasRole(object);
            names.add(role.getName());
            roleIds.add(object.getId());
        });

        while (roleIds.size() > 0) {
            List<String> rs = new ArrayList<>();
            rs.addAll(roleIds);
            repeatRoleIds.addAll(roleIds);
            roleIds.removeAll(roleIds);
            query = new BaasQuery();
            Map<String, Object> ins = new HashMap<>();
            ins.put("$in", rs);
            query.put("roles", ins);
            List<BaasObject> objects = objectService.find(appId, plat, RoleService.ROLE_CLASS_NAME, query, null, null, null, 1000, 0, null, true);
            objects.parallelStream().forEach(object -> {
                BaasRole role = new BaasRole(object);
                names.add(role.getName());
                roleIds.add(role.getId());
            });
            roleIds.removeAll(repeatRoleIds);
        }
        return names;
    }

    /**
     *  根据字符串（对应acl中的key）检查是否有写权限
     *
     * @param acl  acl
     * @param name 字符串
     * @return 检查结果
     */
    private boolean checkWriteAccess(BaasAcl acl, String name) {
        Map<String, Boolean> map = acl.getAccessMap(name);
        Boolean write = map.get("write");
        if (write == null) {
            return false;
        } else {
            return write;
        }
    }

    /**
     *  检查acl中是否有和角色有关的键值对
     *
     * @param acl acl
     * @return 检查结果
     */
    private boolean checkHasRoleAccess(BaasAcl acl) {
        Set<String> keys = acl.keySet();
        boolean flag = false;
        for (String key : keys) {
            if (key.startsWith("role:")) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     *  检查acl中是否存在全员的写权限
     *
     * @param acl acl
     * @return 检查结果
     */
    private boolean checkPublicWriteAccess(BaasAcl acl) {
        return checkWriteAccess(acl, "*");
    }

}
