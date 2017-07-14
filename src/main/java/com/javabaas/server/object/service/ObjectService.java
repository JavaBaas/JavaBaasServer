package com.javabaas.server.object.service;

import com.javabaas.server.admin.entity.*;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.admin.service.StatService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.file.entity.BaasFile;
import com.javabaas.server.file.service.FileService;
import com.javabaas.server.hook.service.HookService;
import com.javabaas.server.object.dao.IDao;
import com.javabaas.server.object.dao.impl.mongo.MongoDao;
import com.javabaas.server.object.entity.*;
import com.javabaas.server.object.entity.error.FieldRequiredError;
import com.javabaas.server.object.entity.error.FieldTypeError;
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
    private ClazzService clazzService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private FileService fileService;
    @Autowired
    private HookService hookService;
    @Autowired
    private StatService statService;
    @Resource(type = MongoDao.class)
    private IDao dao;
    @Autowired
    private JSONUtil jsonUtil;

    public BaasObject insert(String appId, String plat, String className, BaasObject object, BaasUser currentUser, boolean isMaster) {
        //验证表级ACL
        verifyClazzAccess(appId, ClazzAclMethod.INSERT, className, currentUser, isMaster);
        //钩子处理
        hookService.beforeInsert(appId, className, object, currentUser);
        //获取字段
        List<Field> fields = fieldService.list(appId, className);
        //验证数据
        object = verifyObject(appId, fields, object, false, isMaster);
        //设置id
        String id = createId();
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
        return object;
    }

    public void delete(String appId, String plat, String className, String id, BaasUser currentUser, boolean isMaster) {
        //判断id是否合法
        if (!isValidId(id)) {
            throw new SimpleError(SimpleCode.OBJECT_ID_ERROR);
        }
        //查询已经存在的对象
        BaasObject exist = getObject(appId, className, id, isMaster);
        if (exist == null) {
            //对象不存在
            throw new SimpleError(SimpleCode.OBJECT_NOT_EXIST);
        }
        //验证表级ACL
        verifyClazzAccess(appId, ClazzAclMethod.DELETE, className, currentUser, isMaster);
        //验证对象ACL权限
        if (!isMaster) {
            //非master权限验证acl
            BaasAcl acl = exist.getAcl();
            if (!acl.hasWriteAccess(currentUser)) {
                //无操作权限
                throw new SimpleError(SimpleCode.OBJECT_NO_ACCESS);
            }
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
        List<BaasObject> objects = list(appId, plat, className, query, null, null, 1000, 0, currentUser, isMaster);
        for (BaasObject object : objects) {
            delete(appId, plat, className, object.getId(), currentUser, isMaster);
        }
    }

    public void deleteAll(String appId, String className) {
        dao.removeClass(appId, className);
    }

    public void update(String appId, String plat, String className, String id, BaasObject object, BaasUser currentUser, boolean isMaster) {
        //查询已经存在的对象
        BaasObject exist = getObject(appId, className, id, isMaster);
        if (exist == null) {
            //对象不存在
            throw new SimpleError(SimpleCode.OBJECT_NOT_EXIST);
        }
        //判断id是否合法
        if (!isValidId(id)) {
            throw new SimpleError(SimpleCode.OBJECT_ID_ERROR);
        }
        //验证表级ACL
        verifyClazzAccess(appId, ClazzAclMethod.UPDATE, className, currentUser, isMaster);
        //验证对象ACL权限
        if (!isMaster) {
            //非master权限验证acl
            BaasAcl acl = exist.getAcl();
            if (!acl.hasWriteAccess(currentUser)) {
                //无操作权限
                throw new SimpleError(SimpleCode.OBJECT_NO_ACCESS);
            }
        }
        //钩子处理
        hookService.beforeUpdate(appId, className, object, currentUser);
        //获取字段信息
        List<Field> fields = fieldService.list(appId, className);
        object = verifyObject(appId, fields, object, true, isMaster);
        //设置时间
        Date date = new Date();
        long time = date.getTime();
        object.put("updatedAt", time);
        //设置plat
        object.put("updatedPlat", plat);
        //更新
        dao.update(appId, className, new BaasQuery("_id", id), object);
        //统计
        statService.add(new ApiStat(appId, plat, className, ApiMethod.UPDATE, date));
        //钩子处理
        hookService.afterUpdate(appId, className, object, currentUser);
    }

    public void increment(String appId, String plat, String className, String id, BaasObject object, BaasUser currentUser, boolean
            isMaster) {
        //查询已经存在的对象
        BaasObject exist = getObject(appId, className, id, isMaster);
        if (exist == null) {
            //对象不存在
            throw new SimpleError(SimpleCode.OBJECT_NOT_EXIST);
        }
        //验证表级ACL
        verifyClazzAccess(appId, ClazzAclMethod.UPDATE, className, currentUser, isMaster);
        //验证对象ACL权限
        if (!isMaster) {
            //非master权限验证acl
            BaasAcl acl = exist.getAcl();
            if (acl != null && !acl.hasWriteAccess(currentUser)) {
                //无操作权限
                throw new SimpleError(SimpleCode.OBJECT_NO_ACCESS);
            }
        }
        //判断类是否存在
        List<Field> fields = fieldService.list(appId, className);
        object = verifyIncrement(appId, fields, object, isMaster);
        //设置plat
        object.put("updatedPlat", plat);
        //设置时间
        Date date = new Date();
        long time = date.getTime();
        object.put("updatedAt", time);
        //更新对象
        dao.increment(appId, className, new BaasQuery("_id", id), object);
        //统计
        statService.add(new ApiStat(appId, plat, className, ApiMethod.INSERT, date));
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
        return get(appId, plat, className, id, null, null, true);
    }

    public BaasObject get(String appId, String plat, String className, String id, BaasInclude include, BaasUser currentUser, boolean
            isMaster) {
        //判断id是否合法
        if (!isValidId(id)) {
            throw new SimpleError(SimpleCode.OBJECT_ID_ERROR);
        }
        //构建查询
        BaasQuery query = new BaasQuery();
        query.put("_id", id);
        List<BaasObject> result = list(appId, plat, className, ClazzAclMethod.GET, query, null, include, 1, 0, currentUser, isMaster);
        if (result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public List<BaasObject> list(String appId, String plat, String className, ClazzAclMethod method, BaasQuery query, BaasSort sort,
                                 BaasInclude include, int limit, int skip, BaasUser currentUser, boolean isMaster) {
        //上限不得超过1000
        limit = limit > 1000 ? 1000 : limit;
        //limit必须为正数
        if (limit <= 0) limit = 100;
        //验证表级ACL
        verifyClazzAccess(appId, method, className, currentUser, isMaster);
        //查询条件
        if (query == null) {
            query = new BaasQuery();
        }
        //处理子查询
        handleSubQuery(appId, currentUser, isMaster, query);
        //获取根数据
        Map<String, BaasObject> resultMap = getObjects(appId, className, query, sort, limit, skip, currentUser, isMaster);
        List<BaasObject> result = new ArrayList<>();
        if (resultMap == null) {
            return result;
        }
        result.addAll(resultMap.values());
        //处理包含数据
        if (result.size() > 0 && include != null && include.getSubs().size() > 0) {
            //获取包含数据
            include.getSubs().forEach((name, i) -> handleInclude(appId, result, i, currentUser, isMaster));
        }
        //统计
        statService.add(new ApiStat(appId, plat, className, ApiMethod.FIND, new Date()));
        return result;
    }

    public List<BaasObject> list(String appId, String plat, String className, BaasQuery query, BaasSort sort, BaasInclude include,
                                 int limit, int skip, BaasUser currentUser, boolean isMaster) {
        return list(appId, plat, className, ClazzAclMethod.FIND, query, sort, include, limit, skip, currentUser, isMaster);
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
        Map<String, BaasObject> subs = getObjects(appId, (String) searchClass, new BaasQuery((Map) where), null, 1000, null, user,
                isMaster);
        if (subs != null) {
            //用查询结果$in替换$sub
            BaasList list = new BaasList();
            //查询目标类型
            Object targetClass = sub.get("targetClass");
            //查询目标字段
            Object searchKey = sub.get("searchKey");
            if (searchKey == null || targetClass == null) {
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
        return object == null ? null : extractObject(fields, object, isMaster);
    }

    private Map<String, BaasObject> getObjects(String appId, String className, BaasQuery query, BaasUser user, boolean isMaster) {
        return getObjects(appId, className, query, null, null, null, user, isMaster);
    }

    private Map<String, BaasObject> getObjects(String appId, String className, BaasQuery query, BaasSort sort, Integer limit, Integer
            skip, BaasUser user, boolean isMaster) {
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
        query = handleAcl(query, user, isMaster);
        List<BaasObject> objects = dao.find(appId, className, query, sort, limit, skip);
        Map<String, BaasObject> result = new LinkedHashMap<>();
        objects.forEach(obj -> result.put(obj.getId(), extractObject(fields, obj, isMaster)));
        return result;
    }

    public long count(String appId, String className, BaasQuery query, BaasUser currentUser, boolean isMaster) {
        //验证表级ACL
        verifyClazzAccess(appId, ClazzAclMethod.FIND, className, currentUser, isMaster);
        //处理子查询
        handleSubQuery(appId, currentUser, isMaster, query);
        query = handleAcl(query, currentUser, isMaster);
        return dao.count(appId, className, query);
    }

    /**
     * 删除某个字段的所有数据
     */
    public void deleteFieldData(String appId, String className, String fieldName) {
        dao.removeField(appId, className, fieldName);
    }

    private BaasObject extractObject(List<Field> fields, BaasObject object, boolean isMaster) {
        BaasObject extracted = new BaasObject();
        //获取id
        Object id = object.get("_id");
        if (id == null) {
            return null;
        }
        extracted.put("_id", id.toString());
        //获取时间
        Object createdAt = object.get("createdAt");
        Object updatedAt = object.get("updatedAt");
        if (createdAt == null || updatedAt == null) {
            return null;
        }
        extracted.put("createdAt", object.get("createdAt"));
        extracted.put("updatedAt", object.get("updatedAt"));
        //获取plat
        extracted.put("createdPlat", object.get("createdPlat"));
        extracted.put("updatedPlat", object.get("updatedPlat"));
        //获取ACL
        Object acl = object.get("acl");
        if (acl == null) {
            return null;
        }
        extracted.put("acl", new BaasAcl((BaasObject) acl));
        //自定义字段
        fields.forEach(field -> {
            if (!field.isSecurity() || isMaster) {
                //非管理权限 无法操作保密字段
                String name = field.getName();
                Object value = object.get(name);
                if (value != null) {
                    extracted.put(name, value);
                }
            }
        });
        return extracted;
    }

    private BaasObject verifyIncrement(String appId, List<Field> fields, BaasObject object, boolean isMaster) {
        BaasObject verified = new BaasObject();
        for (Field field : fields) {
            if (!field.isSecurity() || isMaster) {
                //非管理权限 无法操作保密字段
                String key = field.getName();
                Object value;
                int type = field.getType();
                value = object.get(key);
                if (value != null) {
                    //验证数据类型
                    switch (type) {
                        case FieldType.NUMBER:
                            //只有数据型字段可以已经自增操作
                            if (!(value instanceof Number)) {
                                throw new FieldTypeError("字段: " + key + " 类型错误。需要数值型，实际为" + value.getClass().getSimpleName() + "。");
                            }
                            verified.put(key, value);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return verified;
    }

    private BaasObject verifyObject(String appId, List<Field> fields, BaasObject object, boolean isUpdate, boolean isMaster) {
        BaasObject verified = new BaasObject();
        for (Field field : fields) {
            if (!field.isSecurity() || isMaster) {
                //非管理权限 无法操作保密字段
                String key = field.getName();
                Object value;
                int type = field.getType();
                value = object.get(key);
                if (field.isRequired()) {
                    //非空字段 新建时不能为空 更新时不能删除
                    if (!isUpdate && StringUtils.isEmpty(value)) {
                        //新建时不能为空
                        throw new FieldRequiredError(key);
                    }
                    if (isUpdate && value != null && value.equals("")) {
                        //更新时不能删除
                        throw new FieldRequiredError(key);
                    }
                }
                if (value != null) {
                    if (isUpdate && value instanceof String && StringUtils.isEmpty(value)) {
                        //更新时，若输入为空字符串，则认为是抹除字段数据。
                        verified.put(key, value);
                    } else {
                        //验证数据类型
                        switch (type) {
                            case FieldType.STRING:
                                if (!(value instanceof String)) {
                                    throw new FieldTypeError("字段: " + key + " 类型错误。需要字符型，实际为" + value.getClass().getSimpleName() + "。");
                                }
                                break;
                            case FieldType.NUMBER:
                                if (!(value instanceof Number)) {
                                    throw new FieldTypeError("字段: " + key + " 类型错误。需要数值型，实际为" + value.getClass().getSimpleName() + "。");
                                }
                                break;
                            case FieldType.BOOLEAN:
                                if (!(value instanceof Boolean)) {
                                    throw new FieldTypeError("字段: " + key + " 类型错误。需要布尔型，实际为" + value.getClass().getSimpleName() + "。");
                                }
                                break;
                            case FieldType.DATE:
                                if (!(value instanceof Number)) {
                                    throw new FieldTypeError("字段: " + key + " 类型错误。日期字段需要数值型，实际为" + value.getClass().getSimpleName() + "。");
                                }
                                break;
                            case FieldType.FILE:
                                if (!(value instanceof Map) || ((Map) value).get("__type") == null
                                        || ((Map) value).get("__type") != null && !((Map) value).get("__type").equals("File")) {
                                    throw new FieldTypeError("字段: " + key + " 类型错误。需要文件型，实际为" + value.getClass().getSimpleName() + "。");
                                } else {
                                    //根据文件ID填充文件信息
                                    String id = (String) ((Map) value).get("_id");
                                    if (StringUtils.isEmpty(id)) {
                                        throw new FieldTypeError("字段: " + key + " 错误。缺失id。");
                                    }
                                    BaasFile file = fileService.getFile(appId, null, id);
                                    if (file == null) {
                                        //文件不存在
                                        throw new SimpleError(SimpleCode.OBJECT_FILE_NOT_FOUND);
                                    } else {
                                        ((Map) value).put("url", file.getUrl());
                                        ((Map) value).put("name", file.getName());
                                    }
                                }
                                break;
                            case FieldType.POINTER:
                                if (!(value instanceof Map) || ((Map) value).get("__type") == null
                                        || ((Map) value).get("__type") != null && !((Map) value).get("__type").equals("Pointer")) {
                                    throw new FieldTypeError("字段: " + key + " 类型错误。需要指针型，实际为" + value.getClass().getSimpleName() + "。");
                                } else {
                                    Object id = ((Map) value).get("_id");
                                    Object className = ((Map) value).get("className");
                                    if (StringUtils.isEmpty(id) || StringUtils.isEmpty(className)) {
                                        throw new FieldTypeError("字段: " + key + " 错误。缺失id或className。");
                                    }
                                    if (!(id instanceof String) || !(className instanceof String)) {
                                        throw new FieldTypeError("字段: " + key + " 类型错误。id或className需要为字符型。");
                                    }
                                    //检查类是否存在
                                    clazzService.get(appId, (String) className);
                                    //调整存储顺序
                                    Map<String, Object> sorted = new LinkedHashMap<>();
                                    sorted.put("__type", "Pointer");
                                    sorted.put("className", className);
                                    sorted.put("_id", id);
                                    value = sorted;
                                }
                                break;
                            case FieldType.OBJECT:
                                if (!(value instanceof Map)) {
                                    throw new FieldTypeError("字段: " + key + " 类型错误。需要对象类型，实际为" + value.getClass().getSimpleName() + "。");
                                }
                                break;
                            case FieldType.ARRAY:
                                if (!(value instanceof List)) {
                                    throw new FieldTypeError("字段: " + key + " 类型错误。需要数组类型，实际为" + value.getClass().getSimpleName() + "。");
                                }
                                break;
                            default:
                                break;
                        }
                        verified.put(key, value);
                    }
                }
            }
        }
        //处理ACL
        if (isUpdate) {
            Object value = object.get("acl");
            if (value != null) {
                //修改ACL
                if (!(value instanceof Map)) {
                    throw new FieldTypeError("ACL格式错误");
                }
                verified.put("acl", value);
            }
        } else {
            Object value = object.get("acl");
            if (value == null) {
                //默认全局读写
                BaasAcl acl = new BaasAcl();
                acl.setPublicReadAccess(true);
                acl.setPublicWriteAccess(true);
                verified.put("acl", acl);
            } else {
                if (!(value instanceof Map)) {
                    throw new FieldTypeError("ACL格式错误");
                }
                verified.put("acl", value);
            }
        }
        return verified;
    }

    private void verifyClazzAccess(String appId, ClazzAclMethod method, String className, BaasUser user, boolean isMaster) {
        Clazz clazz = clazzService.get(appId, className);
        if (clazz.getAcl() != null) {
            //验证表级ACL权限
            if (!isMaster) {
                //非master权限验证acl
                if (!clazz.getAcl().hasAccess(method, user)) {
                    //无操作权限
                    throw new SimpleError(SimpleCode.OBJECT_CLAZZ_NO_ACCESS);
                }
            }
        }
    }

    private BaasQuery handleAcl(BaasQuery query, BaasUser user, boolean isMaster) {
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
     * 整理包含字段
     * <br>
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

    private String createId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private boolean isValidId(String id) {
        return !StringUtils.isEmpty(id) && id.length() == 32;
    }

}