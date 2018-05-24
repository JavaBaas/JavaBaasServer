package com.javabaas.server.object.service;

import com.javabaas.server.admin.entity.*;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.admin.service.StatService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.hook.service.HookService;
import com.javabaas.server.object.dao.IDao;
import com.javabaas.server.object.dao.impl.mongo.MongoDao;
import com.javabaas.server.object.entity.*;
import com.javabaas.server.object.util.BaasObjectIdUtil;
import com.javabaas.server.user.entity.BaasUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * 对象操作服务
 * Created by Staryet on 15/6/19.
 */
@Service
public class ObjectService {

    @Autowired
    private FieldService fieldService;
    @Autowired
    private HookService hookService;
    @Autowired
    private StatService statService;
    @Autowired
    private AclHandler aclHandler;
    @Autowired
    private ObjectChecker objectChecker;
    @Autowired
    private ObjectExtractor objectExtractor;
    @Autowired
    private ClazzAclChecker clazzAclChecker;
    @Resource(type = MongoDao.class)
    private IDao dao;

    public BaasObject insert(String appId, String plat, String className, BaasObject object, BaasUser currentUser, boolean isMaster) {
        return insert(appId, plat, className, object, false, currentUser, isMaster);
    }

    /**
     * 插入对象
     *
     * @param className   类名称
     * @param object      要插入的对象
     * @param fetch       是否返回对象信息
     * @param currentUser 当前用户
     * @param isMaster    超级权限
     * @return 新创建的对象
     */
    public BaasObject insert(String appId, String plat, String className, BaasObject object, boolean fetch, BaasUser currentUser, boolean
            isMaster) {
        //验证表级ACL
        clazzAclChecker.verifyClazzAccess(appId, ClazzAclMethod.INSERT, className, currentUser, isMaster);
        //钩子处理
        hookService.beforeInsert(appId, className, object, currentUser);
        //验证数据
        object = objectChecker.checkInsert(appId, className, object, isMaster);
        //设置id
        String id = BaasObjectIdUtil.createId();
        object.setId(id);
        //设置时间
        Date date = new Date();
        long time = date.getTime();
        object.put("createdAt", time);
        object.put("updatedAt", time);
        //设置platform
        object.put("createdPlat", plat);
        object.put("updatedPlat", plat);
        //插入操作
        dao.insert(appId, className, object);
        //统计
        statService.add(new ApiStat(appId, plat, className, ApiMethod.INSERT, date));
        //钩子处理
        hookService.afterInsert(appId, className, object, currentUser);
        if (fetch) {
            //返回完整数据
            object = get(appId, plat, className, id, null, null, currentUser, isMaster);
        } else {
            //只返回对象id 创建时间
            object = new BaasObject();
            object.setId(id);
            object.put("createdAt", time);
        }
        return object;
    }

    /**
     * 删除对象
     *
     * @param className   类名称
     * @param id          id
     * @param currentUser 当前用户
     * @param isMaster    超级权限
     */
    public void delete(String appId, String plat, String className, String id, BaasUser currentUser, boolean isMaster) {
        //判断id是否合法
        if (!BaasObjectIdUtil.isValidId(id)) {
            throw new SimpleError(SimpleCode.OBJECT_ID_ERROR);
        }
        //查询已经存在的对象
        BaasObject exist = getObject(appId, className, id, isMaster);
        if (exist == null) {
            //对象不存在
            throw new SimpleError(SimpleCode.OBJECT_NOT_EXIST);
        }
        //验证表级ACL
        clazzAclChecker.verifyClazzAccess(appId, ClazzAclMethod.DELETE, className, currentUser, isMaster);
        //验证对象ACL权限
        BaasAcl acl = exist.getAcl();
        if (!aclHandler.checkWriteAccess(appId, acl, currentUser, isMaster)) {
            //无操作权限
            throw new SimpleError(SimpleCode.OBJECT_NO_ACCESS);
        }
        //钩子处理
        hookService.beforeDelete(appId, className, exist, currentUser);
        //删除对象
        dao.remove(appId, className, new BaasQuery("_id", id));
        //统计
        statService.add(new ApiStat(appId, plat, className, ApiMethod.DELETE, new Date()));
        //钩子处理
        hookService.afterDelete(appId, className, exist, currentUser);
    }

    public void deleteByQuery(String appId, String plat, String className, BaasQuery query, BaasUser currentUser, boolean isMaster) {
        List<BaasObject> objects = find(appId, plat, className, query, null, null, null, 1000, 0, currentUser, isMaster);
        for (BaasObject object : objects) {
            delete(appId, plat, className, object.getId(), currentUser, isMaster);
        }
    }

    public void deleteAll(String appId, String className) {
        dao.removeClass(appId, className);
    }

    public long update(String appId, String plat, String className, String id, BaasObject object, BaasUser currentUser,
                       boolean isMaster) {
        return update(appId, plat, className, id, null, object, currentUser, isMaster);
    }

    /**
     * 更新对象
     *
     * @param className   类名称
     * @param id          id
     * @param query       查询条件(此值不为空时 需检查查询条件 满足条件才进行更新)
     * @param object      对象
     * @param currentUser 当前用户
     * @param isMaster    管理权限
     */
    public long update(String appId, String plat, String className, String id, BaasQuery query, BaasObject object, BaasUser currentUser,
                       boolean isMaster) {
        //判断id是否合法
        if (!BaasObjectIdUtil.isValidId(id)) {
            throw new SimpleError(SimpleCode.OBJECT_ID_ERROR);
        }
        //查询已经存在的对象
        BaasObject exist = getObject(appId, className, id, isMaster);
        if (exist == null) {
            //对象不存在
            throw new SimpleError(SimpleCode.OBJECT_NOT_EXIST);
        }
        //验证表级ACL
        clazzAclChecker.verifyClazzAccess(appId, ClazzAclMethod.UPDATE, className, currentUser, isMaster);
        //验证对象ACL权限
        BaasAcl acl = exist.getAcl();
        if (!aclHandler.checkWriteAccess(appId, acl, currentUser, isMaster)) {
            //无操作权限
            throw new SimpleError(SimpleCode.OBJECT_NO_ACCESS);
        }
        //钩子处理
        hookService.beforeUpdate(appId, className, object, currentUser);
        //验证数据
        object = objectChecker.checkUpdate(appId, className, object, isMaster);
        //设置时间
        Date date = new Date();
        long time = date.getTime();
        object.put("updatedAt", time);
        //设置plat
        object.put("updatedPlat", plat);
        //更新
        if (query == null) {
            //更新条件为空 默认使用update
            dao.update(appId, className, new BaasQuery("_id", id), object);
        } else {
            //更新条件非空 使用findAndModify更新
            query.put("_id", id);
            dao.findAndModify(appId, className, query, object);
        }
        //统计
        statService.add(new ApiStat(appId, plat, className, ApiMethod.UPDATE, date));
        //钩子处理
        hookService.afterUpdate(appId, className, object, currentUser);
        //返回更新时间
        return time;
    }

    /**
     * 获取对象
     * 注意:此处使用了管理权限
     *
     * @param appId     应用id
     * @param className 类名称
     * @param id        对象id
     * @return 对象
     */
    public BaasObject get(String appId, String plat, String className, String id) {
        return get(appId, plat, className, id, null, null, null, true);
    }

    public BaasObject get(String appId, String plat, String className, String id, BaasInclude include, BaasList keys, BaasUser currentUser,
                          boolean
                                  isMaster) {
        //判断id是否合法
        if (!BaasObjectIdUtil.isValidId(id)) {
            throw new SimpleError(SimpleCode.OBJECT_ID_ERROR);
        }
        //构建查询
        BaasQuery query = new BaasQuery();
        query.put("_id", id);
        List<BaasObject> result = findInternal(appId, plat, className, ClazzAclMethod.GET, query, null, include, keys, 1, 0, currentUser,
                isMaster);
        if (result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public List<BaasObject> find(String appId, String plat, String className, BaasQuery query, BaasSort sort, BaasInclude include,
                                 BaasList keys, int limit, int skip, BaasUser currentUser, boolean isMaster) {
        return findInternal(appId, plat, className, ClazzAclMethod.FIND, query, sort, include, keys, limit, skip, currentUser, isMaster);
    }

    private List<BaasObject> findInternal(String appId, String plat, String className, ClazzAclMethod method, BaasQuery query, BaasSort
            sort, BaasInclude include, BaasList keys, int limit, int skip, BaasUser currentUser, boolean isMaster) {
        //上限不得超过1000
        limit = limit > 1000 ? 1000 : limit;
        //limit必须为正数
        if (limit <= 0) limit = 100;
        //验证表级ACL
        clazzAclChecker.verifyClazzAccess(appId, method, className, currentUser, isMaster);
        //查询条件
        if (query == null) {
            query = new BaasQuery();
        }
        //处理子查询
        handleSubQuery(appId, currentUser, isMaster, query);
        //获取根数据
        Map<String, BaasObject> resultMap = getObjects(appId, className, query, keys, sort, limit, skip, currentUser, isMaster);
        List<BaasObject> result = new ArrayList<>();
        if (resultMap == null) {
            //查询结果为空
            return result;
        }
        result.addAll(resultMap.values());
        //处理包含数据
        handleIncludes(appId, result, include, currentUser, isMaster);
        //统计
        statService.add(new ApiStat(appId, plat, className, ApiMethod.FIND, new Date()));
        return result;
    }

    private void handleSubQuery(String appId, BaasUser user, boolean isMaster, BaasQuery query) {
        if (query != null && query.size() > 0) {
            handleSub(appId, user, isMaster, null, null, query);
        }
    }

    private boolean handleSub(String appId, BaasUser user, boolean isMaster, String field, Map<String, Object> parent, Map<String,
            Object> query) {
        boolean flag = false;
        boolean sub = false;
        Set<Map.Entry<String, Object>> entries = query.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals("$sub")) {
                flag = true;
            }
            if (entry.getValue() instanceof Map) {
                sub = handleSub(appId, user, isMaster, key, query, (Map<String, Object>) value) || sub;
            }
        }
        //子节点循环完毕
        //返回后判断无子节点且节点名称为$sub则进行子查询处理
        if ("$sub".equals(field) && !sub) {
            replaceSub(appId, user, isMaster, parent, query);
        }
        return flag;
    }

    /**
     * 使用查询结果替换$sub中的内容
     */
    private void replaceSub(String appId, BaasUser user, boolean isMaster, Map parent, Map sub) {
        //找到子查询节点
        //子查询的查询条件
        Object where = sub.get("where");
        if (where == null || !(where instanceof Map)) {
            throw new SimpleError(SimpleCode.OBJECT_SUB_QUERY_EMPTY_WHERE);
        }
        //子查询的类
        Object searchClass = sub.get("searchClass");
        if (StringUtils.isEmpty(searchClass) || !(searchClass instanceof String)) {
            throw new SimpleError(SimpleCode.OBJECT_SUB_QUERY_EMPTY_SEARCH_CLASS);
        }
        Map<String, BaasObject> subs = getObjects(appId, (String) searchClass, new BaasQuery((Map) where), null, null, 1000, null, user,
                isMaster);
        if (subs != null) {
            //用查询结果$in替换$sub
            BaasList list = new BaasList();
            //查询目标类型
            Object targetClass = sub.get("targetClass");
            //查询目标字段
            Object searchKey = sub.get("searchKey");
            if (searchKey == null && targetClass == null) {
                //子查询
                Set<String> keys = subs.keySet();
                //直接将查询结果的id添加至主查询
                for (String key : keys) {
                    BaasObject object = new BaasObject();
                    object.put("__type", "Pointer");
                    object.put("className", searchClass);
                    object.setId(key);
                    list.add(object);
                }
            } else if (searchKey != null && targetClass == null) {
                subs.forEach((key, value) -> {
                    BaasObject targetObject = (BaasObject) value.get(searchKey.toString());
                    list.add(targetObject.getId());
                });
            } else {
                //匹配查询
                for (Map.Entry<String, BaasObject> resultEntry : subs.entrySet()) {
                    BaasObject source = resultEntry.getValue();
                    BaasObject object = new BaasObject();
                    object.put("__type", "Pointer");
                    object.put("className", targetClass);
                    //将查询结果对应字段的对象id添加至主查询
                    BaasObject targetObject = (BaasObject) source.get(searchKey.toString());
                    object.setId(targetObject.getId());
                    list.add(object);
                }
            }
            //移除$sub节点
            parent.remove("$sub");
            //添加$in节点
            parent.put("$in", list);
        }
    }

    private void handleIncludes(String appId, List<BaasObject> objects, BaasInclude include, BaasUser user, boolean isMaster) {
        if (objects.size() > 0 && include != null && include.getSubs().size() > 0) {
            //获取包含数据
            include.getSubs().forEach((name, i) -> handleInclude(appId, objects, i, user, isMaster));
        }
    }

    private void handleInclude(String appId, List<BaasObject> objects, BaasInclude include, BaasUser user, boolean isMaster) {
        try {
            //整理所需的类和id
            ClassIds ids = new ClassIds();
            for (BaasObject o : objects) {
                try {
                    BaasObject object = (BaasObject) o.get(include.getName());
                    if (object != null) {
                        String type = object.getString("__type");
                        String className = object.getString("className");
                        String id = object.getString("_id");
                        if (type.equals("Pointer") && !StringUtils.isEmpty(className) && !StringUtils.isEmpty(id)) {
                            ids.addId(className, id);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            //查询需要关联的类
            Set<String> classNames = ids.getClassNames();
            //子列表
            List<BaasObject> subList = new ArrayList<>();
            for (String className : classNames) {
                //查询对应的对象列表
                List<String> objectIds = ids.getIds(className);
                //被关联的对象
                Map<String, BaasObject> subObjects = getObjects(appId, className, new BaasQuery("_id", new BaasObject("$in", objectIds)),
                        user, isMaster);
                if (subObjects != null && subObjects.size() > 0) {
                    //添加到对应对象
                    for (BaasObject o : objects) {
                        BaasObject object = (BaasObject) o.get(include.getName());
                        if (object != null) {
                            String id = object.getString("_id");
                            BaasObject subObject = subObjects.get(id);
                            if (subObject != null) {
                                object.putAll(subObject);
                                //添加到需要处理的子列表中
                                subList.add(subObject);
                            }
                        }
                    }
                }
            }
            if (subList.size() > 0 && include.getSubs().size() > 0) {
                //处理下一级include
                Map<String, BaasInclude> subs = include.getSubs();
                subs.forEach((name, i) -> handleInclude(appId, subList, i, user, isMaster));
            }
        } catch (SimpleError ignored) {
        }
    }

    /**
     * 忽略权限获取对象(主要用于判断对象是否存在)
     *
     * @param className 类名称
     * @param id        id
     * @return 对象
     */
    private BaasObject getObject(String appId, String className, String id, boolean isMaster) {
        List<Field> fields = fieldService.list(appId, className);
        BaasObject object = dao.findOne(appId, className, new BaasQuery("_id", id));
        return object == null ? null : objectExtractor.extractObject(fields, object, isMaster);
    }

    private Map<String, BaasObject> getObjects(String appId, String className, BaasQuery query, BaasUser user, boolean isMaster) {
        return getObjects(appId, className, query, null, null, null, null, user, isMaster);
    }

    private Map<String, BaasObject> getObjects(String appId, String className, BaasQuery query, BaasList keys, BaasSort sort, Integer limit,
                                               Integer skip, BaasUser user, boolean isMaster) {
        //排序条件
        if (sort == null) {
            //默认排序为更新时间倒序
            sort = new BaasSort("updatedAt", -1);
        }
        //获取字段描述
        List<Field> fields;
        try {
            fields = fieldService.list(appId, className);
        } catch (SimpleError error) {
            return null;
        }
        //处理ACL权限
        query = aclHandler.handleAcl(appId, query, user, isMaster);
        if (keys != null) {
            //处理keys 添加默认字段
            keys.addAll(InternalFields.fields());
        }
        List<BaasObject> objects = dao.find(appId, className, query, keys, sort, limit, skip);
        Map<String, BaasObject> result = new LinkedHashMap<>();
        objects.forEach(obj -> result.put(obj.getId(), objectExtractor.extractObject(fields, obj, isMaster)));
        return result;
    }

    public long count(String appId, String className, BaasQuery query, BaasUser currentUser, boolean isMaster) {
        //验证表级ACL
        clazzAclChecker.verifyClazzAccess(appId, ClazzAclMethod.FIND, className, currentUser, isMaster);
        //处理子查询
        handleSubQuery(appId, currentUser, isMaster, query);
        query = aclHandler.handleAcl(appId, query, currentUser, isMaster);
        return dao.count(appId, className, query);
    }

    /**
     * 删除某个字段的所有数据
     */
    public void deleteFieldData(String appId, String className, String fieldName) {
        dao.removeField(appId, className, fieldName);
    }

    /**
     * 整理包含字段
     * 由字符串描述模式转换为对象树模式
     *
     * @param include 引用字段 如:a,a.b,a.c,d
     * @return 包含
     */
    public BaasInclude getBaasInclude(String include) {
        if (StringUtils.isEmpty(include)) {
            return null;
        } else {
            //创建根节点
            BaasInclude root = new BaasInclude("root");
            BaasInclude now = root;
            String[] includes = include.split(",");
            for (String s : includes) {
                String[] notes = s.split("\\.");
                for (String n : notes) {
                    if (StringUtils.isEmpty(n)) {
                        //错误信息
                        break;
                    }
                    BaasInclude sub = now.getSub(n);
                    if (sub == null) {
                        sub = new BaasInclude(n);
                        now.addSub(sub);
                    }
                    now = sub;
                }
                now = root;
            }
            return root;
        }
    }

}