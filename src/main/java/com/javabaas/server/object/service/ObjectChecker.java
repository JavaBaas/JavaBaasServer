package com.javabaas.server.object.service;

import com.javabaas.server.admin.entity.Field;
import com.javabaas.server.admin.entity.FieldType;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.file.entity.BaasFile;
import com.javabaas.server.file.service.FileService;
import com.javabaas.server.object.entity.BaasAcl;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasOperator;
import com.javabaas.server.object.entity.BaasOperatorEnum;
import com.javabaas.server.object.entity.error.FieldRequiredError;
import com.javabaas.server.object.entity.error.FieldTypeError;
import com.javabaas.server.object.util.BaasOperatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 对象检查器
 * Created by Codi on 2017/8/3.
 */
@Component
public class ObjectChecker {

    @Autowired
    private FieldService fieldService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private FileService fileService;

    BaasObject checkInsert(String appId, String className, BaasObject object, boolean isMaster) {
        return checkObject(appId, className, object, false, isMaster);
    }

    BaasObject checkUpdate(String appId, String className, BaasObject object, boolean isMaster) {
        return checkObject(appId, className, object, true, isMaster);
    }

    /**
     * 检查并预处理传入的对象
     *
     * @param appId     应用id
     * @param className 类名称
     * @param object    对象
     * @param isUpdate  是否为更新
     * @param isMaster  是否为管理权限
     * @return 处理后的对象
     */
    private BaasObject checkObject(String appId, String className, BaasObject object, boolean isUpdate, boolean isMaster) {
        //获取字段信息
        List<Field> fields = fieldService.list(appId, className);
        //检查后对象
        BaasObject verified = new BaasObject();
        for (Field field : fields) {
            //循环处理所有类中定义的字段
            boolean permission = (!field.isSecurity() && !field.isReadonly()) || isMaster;
            if (permission) {
                //非保密字段且非只读字段或拥有管理权限
                String key = field.getName();
                int type = field.getType();
                Object value = object.get(key);
                if (field.isNotNUll()) {
                    //非空字段 新建时不能为空
                    if (!isUpdate && StringUtils.isEmpty(value)) {
                        //新建时不能为空
                        throw new FieldRequiredError(key);
                    }
                    //非空字段 更新时禁止删除
                    if (isUpdate) {
                        BaasOperator operator = BaasOperatorUtil.getOperator(key, type, value);
                        if (operator != null && operator.getType() == BaasOperatorEnum.DELETE) {
                            throw new FieldRequiredError(key);
                        }
                    }
                }
                if (value != null) {
                    //检查是否有操作符 如果有操作符 用BaasOperator替换原value
                    BaasOperator operator = BaasOperatorUtil.getOperator(key, type, value);
                    if (operator != null) {
                        verified.put(key, operator);
                    } else {
                        //非操作符 检查字段值是否合法
                        Object checked = checkField(appId, type, key, value);
                        //验证成功 将字段写入
                        verified.put(key, checked);
                    }
                }
            }
        }
        checkAcl(object, verified, isUpdate);
        return verified;
    }

    /**
     * 检查字段值是否合法
     *
     * @param appId 应用id
     * @param type  字段类型
     * @param key   字段名
     * @param value 字段值
     */
    private Object checkField(String appId, int type, String key, Object value) {
        switch (type) {
            //验证数据类型
            case FieldType.STRING:
                if (!(value instanceof String)) {
                    throw new FieldTypeError("字段: " + key + " 类型错误。需要字符型，实际为" + value.getClass().getSimpleName() + "。");
                }
                return value;
            case FieldType.NUMBER:
                if (!(value instanceof Number)) {
                    throw new FieldTypeError("字段: " + key + " 类型错误。需要数值型，实际为" + value.getClass().getSimpleName() + "。");
                }
                return value;
            case FieldType.BOOLEAN:
                if (!(value instanceof Boolean)) {
                    throw new FieldTypeError("字段: " + key + " 类型错误。需要布尔型，实际为" + value.getClass().getSimpleName() + "。");
                }
                return value;
            case FieldType.DATE:
                if (!(value instanceof Number)) {
                    throw new FieldTypeError("字段: " + key + " 类型错误。日期字段需要数值型，实际为" + value.getClass().getSimpleName() + "。");
                }
                return value;
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
                return value;
            case FieldType.POINTER:
                if (!(value instanceof Map) || ((Map) value).get("__type") == null
                        || ((Map) value).get("__type") != null && !((Map) value).get("__type").equals("Pointer")) {
                    throw new FieldTypeError("字段: " + key + " 类型错误。需要指针型，实际为" + value.getClass().getSimpleName() + "。");
                } else {
                    Map pointer = (Map) value;
                    Object id = pointer.get("_id");
                    Object pointerClassName = pointer.get("className");
                    if (StringUtils.isEmpty(id) || StringUtils.isEmpty(pointerClassName)) {
                        throw new FieldTypeError("字段: " + key + " 错误。缺失id或className。");
                    }
                    if (!(id instanceof String) || !(pointerClassName instanceof String)) {
                        throw new FieldTypeError("字段: " + key + " 类型错误。id或className需要为字符型。");
                    }
                    //检查类是否存在
                    clazzService.get(appId, (String) pointerClassName);
                    //调整存储顺序
                    Map<String, Object> sorted = new LinkedHashMap<>();
                    sorted.put("__type", "Pointer");
                    sorted.put("className", pointerClassName);
                    sorted.put("_id", id);
                    return sorted;
                }
            case FieldType.OBJECT:
                if (!(value instanceof Map)) {
                    throw new FieldTypeError("字段: " + key + " 类型错误。需要对象类型，实际为" + value.getClass().getSimpleName() + "。");
                }
                return value;
            case FieldType.ARRAY:
                if (!(value instanceof List)) {
                    throw new FieldTypeError("字段: " + key + " 类型错误。需要数组类型，实际为" + value.getClass().getSimpleName() + "。");
                }
                return value;
            case FieldType.GEOPOINT:
                if (!(value instanceof List) || ((List) value).size() != 2 || !(((List) value).get(0) instanceof Number) || !(((List)
                        value).get(1) instanceof Number)) {
                    throw new FieldTypeError("字段: " + key + " 类型错误。需要经纬度坐标对 [经度,纬度] ，实际为" + value.getClass().getSimpleName() + "。");
                } else {
                    List list = (List) value;
                    if (Math.abs(Double.valueOf(list.get(0).toString())) > 180 || Math.abs(Double.valueOf(list.get(1).toString())) > 90) {
                        throw new FieldTypeError("字段: " + key + " 数据错误。经度取值范围[-180,180],纬度取值范围[-90,90]");
                    }
                }
                return value;
            default:
                return value;
        }
    }

    /**
     * 处理ACL字段
     *
     * @param object   源对象
     * @param verified 结果对象
     * @param isUpdate 是否为更新
     */
    private void checkAcl(BaasObject object, BaasObject verified, boolean isUpdate) {
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
    }

}
