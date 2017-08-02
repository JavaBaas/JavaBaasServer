package com.javabaas.server.object.controller;

import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.common.service.MasterService;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.object.entity.BaasInclude;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.object.entity.BaasSort;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.user.entity.BaasUser;
import com.javabaas.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 对象控制器
 * Created by Staryet on 15/6/4.
 */
@RestController
@RequestMapping(value = "/api/object")
public class ObjectController {

    @Autowired
    private ObjectService objectService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private MasterService masterService;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private JSONUtil jsonUtil;

    /**
     * 插入对象
     *
     * @param body 对象
     * @param name 类名
     * @return 结果
     */
    @RequestMapping(value = "/{name}", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult insert(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @RequestParam(required = false) boolean fetch,
                               @RequestBody String body,
                               @PathVariable String name) {
        //处理权限
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        //非master权限禁止操作内部类数据
        if (!isMaster && !clazzService.isNameValid(name)) {
            throw new SimpleError(SimpleCode.CLAZZ_NAME_ERROR);
        }
        BaasObject object = jsonUtil.readValue(body, BaasObject.class);
        //存储对象
        BaasObject newObject = objectService.insert(appId, plat, name, object, fetch, currentUser, isMaster);
        //只有fetch为true时 返回完整对象信息
        SimpleResult result = SimpleResult.success();
        result.putDataAll(newObject);
        return result;
    }

    /**
     * 获取对象
     *
     * @param name 类名
     * @param id   id
     * @return 对象
     */
    @RequestMapping(value = "/{name}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public BaasObject get(@RequestHeader(value = "JB-AppId") String appId,
                          @RequestHeader(value = "JB-Plat") String plat,
                          @PathVariable String name,
                          @PathVariable String id,
                          @RequestParam(required = false) String include) {
        //处理include
        BaasInclude bassInclude = objectService.getBaasInclude(include);
        //处理权限
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        //获取数据
        BaasObject result = objectService.get(appId, plat, name, id, bassInclude, currentUser, isMaster);
        if (result == null) {
            result = new BaasObject();
        }
        return result;
    }

    /**
     * 查询对象
     *
     * @param name  类名
     * @param where 查询条件
     * @return 对象列表
     */
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    @ResponseBody
    public List<BaasObject> find(@RequestHeader(value = "JB-AppId") String appId,
                                 @RequestHeader(value = "JB-Plat") String plat,
                                 @PathVariable String name,
                                 @RequestParam(required = false) String where,
                                 @RequestParam(required = false) String order,
                                 @RequestParam(required = false) String include,
                                 @RequestParam(required = false, defaultValue = "100") int limit,
                                 @RequestParam(required = false, defaultValue = "0") int skip) {
        //处理查询字段
        BaasQuery query = StringUtils.isEmpty(where) ? null : jsonUtil.readValue(where, BaasQuery.class);
        //处理排序字段
        BaasSort sort = StringUtils.isEmpty(order) ? null : jsonUtil.readValue(order, BaasSort.class);
        //处理include
        BaasInclude bassInclude = objectService.getBaasInclude(include);
        //处理权限
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        return objectService.find(appId, plat, name, query, sort, bassInclude, limit, skip, currentUser, isMaster);
    }

    /**
     * 获取对象个数
     *
     * @param name  类名
     * @param where 查询条件
     * @return 个数
     */
    @RequestMapping(value = "/{name}/count", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult count(@RequestHeader(value = "JB-AppId") String appId,
                              @RequestHeader(value = "JB-Plat") String plat,
                              @PathVariable String name,
                              @RequestParam(required = false) String where) {
        BaasQuery query = StringUtils.isEmpty(where) ? null : jsonUtil.readValue(where, BaasQuery.class);
        //处理权限
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        return SimpleResult.success().putData("count", objectService.count(appId, name, query, currentUser, isMaster));
    }

    /**
     * 更新对象
     *
     * @param body 对象
     * @param name 类名
     * @return 结果
     */
    @RequestMapping(value = "/{name}/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult update(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @RequestParam(required = false) String where,
                               @RequestBody Map<String, Object> body,
                               @PathVariable String name,
                               @PathVariable String id) {
        //处理查询字段
        BaasQuery query = StringUtils.isEmpty(where) ? null : jsonUtil.readValue(where, BaasQuery.class);
        //处理权限
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        //非master权限禁止操作内部类对象
        if (!isMaster && !clazzService.isNameValid(name)) {
            throw new SimpleError(SimpleCode.CLAZZ_NAME_ERROR);
        }
        BaasObject object = new BaasObject(body);
        long time = objectService.update(appId, plat, name, id, query, object, currentUser, isMaster);
        return SimpleResult.success().putData("createdAt", time);
    }

    /**
     * 原子操作
     *
     * @param body 对象
     * @param name 类名
     * @return 结果
     */
    @RequestMapping(value = "/{name}/{id}/inc", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult increment(@RequestHeader(value = "JB-AppId") String appId,
                                  @RequestHeader(value = "JB-Plat") String plat,
                                  @RequestBody Map<String, Object> body,
                                  @PathVariable String name,
                                  @PathVariable String id) {
        //处理权限
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        //非master权限禁止操作内部类对象
        if (!isMaster && !clazzService.isNameValid(name)) {
            throw new SimpleError(SimpleCode.CLAZZ_NAME_ERROR);
        }
        BaasObject object = new BaasObject(body);
        objectService.increment(appId, plat, name, id, object, currentUser, isMaster);
        return SimpleResult.success();
    }

    /**
     * 删除对象
     *
     * @param name 类名
     * @param id   id
     * @return 结果
     */
    @RequestMapping(value = "/{name}/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public SimpleResult delete(@RequestHeader(value = "JB-AppId") String appId,
                               @RequestHeader(value = "JB-Plat") String plat,
                               @PathVariable String name,
                               @PathVariable String id) {
        //处理权限
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        //非master权限禁止操作内部类对象
        if (!isMaster && !clazzService.isNameValid(name)) {
            throw new SimpleError(SimpleCode.CLAZZ_NAME_ERROR);
        }
        //处理id
        String[] ids = id.split(",");
        for (String s : ids) {
            //删除
            objectService.delete(appId, plat, name, s, currentUser, isMaster);
        }
        return SimpleResult.success();
    }

    @RequestMapping(value = "/{name}/deleteByQuery", method = RequestMethod.DELETE)
    @ResponseBody
    public SimpleResult deleteByQuery(@RequestHeader(value = "JB-AppId") String appId,
                                      @RequestHeader(value = "JB-Plat") String plat,
                                      @PathVariable String name,
                                      @RequestParam(required = false) String where) {
        //处理查询字段
        BaasQuery query = StringUtils.isEmpty(where) ? null : jsonUtil.readValue(where, BaasQuery.class);
        //处理权限
        boolean isMaster = masterService.isMaster(request);
        BaasUser currentUser = userService.getCurrentUser(appId, plat, request);
        //非master权限禁止操作内部类对象
        if (!isMaster && !clazzService.isNameValid(name)) {
            throw new SimpleError(SimpleCode.CLAZZ_NAME_ERROR);
        }
        objectService.deleteByQuery(appId, plat, name, query, currentUser, isMaster);
        return SimpleResult.success();
    }

}
