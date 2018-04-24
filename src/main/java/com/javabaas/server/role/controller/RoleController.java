package com.javabaas.server.role.controller;

import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.common.sign.AuthChecker;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.role.entity.BaasRole;
import com.javabaas.server.role.service.RoleService;
import com.javabaas.server.user.entity.BaasUser;
import com.javabaas.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zangyilin on 2018/4/16.
 */
@RestController
@RequestMapping(value = "/api/roles/")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthChecker authChecker;
    @Autowired
    private JSONUtil jsonUtil;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserService userService;
    @Autowired
    ObjectService objectService;

    /**
     *  新建角色
     *
     * @param body 角色信息
     * @return 角色信息
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult insert(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @RequestBody String body) {
        BaasRole insertRole = jsonUtil.readBaas(body, BaasRole.class);
        //处理权限
        boolean isMaster = authChecker.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        BaasRole role = roleService.insert(appId, plat, insertRole, currentUser, isMaster);
        SimpleResult result = SimpleResult.success();
        result.putData("result", role);
        return result;
    }

    /**
     *  获取角色
     *
     * @param id 角色Id
     * @return 角色信息
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult get(@RequestHeader(value = "JB-AppId") String appId,
                            @RequestHeader(value = "JB-Plat") String plat,
                            @PathVariable String id) {
        //处理权限
        boolean isMaster = authChecker.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        BaasObject object = objectService.get(appId, plat, RoleService.ROLE_CLASS_NAME, id, null, null, currentUser, isMaster);
        if (object == null) {
            object = new BaasRole();
        }
        SimpleResult result = SimpleResult.success();
        result.putData("result", new BaasRole(object));
        return result;
    }

    /**
     *  更新角色
     *
     * @param id 角色id
     * @param body 更新角色信息
     * @return 更新结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult update(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @PathVariable String id,
                               @RequestBody String body) {
        BaasRole updateRole = jsonUtil.readValue(body, BaasRole.class);
        //处理权限
        boolean isMaster = authChecker.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        roleService.update(appId, plat, id, updateRole, currentUser, isMaster);
        return SimpleResult.success();
    }

    /**
     *  删除角色
     *
     * @param id 角色Id
     * @return 处理结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public SimpleResult delete(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @PathVariable String id) {
        //处理权限
        boolean isMaster = authChecker.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        objectService.delete(appId, plat, RoleService.ROLE_CLASS_NAME, id, currentUser, isMaster);
        return SimpleResult.success();
    }

}
