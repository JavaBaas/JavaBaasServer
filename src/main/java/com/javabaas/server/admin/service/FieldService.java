package com.javabaas.server.admin.service;

import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.entity.Field;
import com.javabaas.server.admin.entity.FieldType;
import com.javabaas.server.admin.entity.dto.FieldExport;
import com.javabaas.server.admin.repository.FieldRepository;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.object.service.ObjectService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 字段处理
 * <p/>
 * Created by Codi on 15/6/20.
 */
@Service
public class FieldService {

    private Log log = LogFactory.getLog(getClass());

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private JSONUtil jsonUtil;

    public void insert(String appId, String clazzName, Field field) {
        String name = field.getName();
        if (StringUtils.isEmpty(name)) {
            throw new SimpleError(SimpleCode.FIELD_NAME_MISSING);
        }
        if (isReserved(name) || !isNameValid(name)) {
            //保留字禁止作为字段名、验证字段拼写是否合法
            throw new SimpleError(SimpleCode.FIELD_NAME_ERROR);
        }
        if (!FieldType.isValid(field.getType())) {
            //检查字段类型是否合法
            throw new SimpleError(SimpleCode.FIELD_TYPE_ERROR);
        }
        Clazz clazz = clazzService.get(appId, clazzName);
        field.setClazz(clazz);
        Field exist = fieldRepository.findByClazzAndName(clazz, name);
        if (exist != null) {
            throw new SimpleError(SimpleCode.FIELD_ALREADY_EXIST);
        }
        field.setId(null);
        fieldRepository.insert(field);
        log.info("App:" + appId + " Class:" + clazzName + " Field:" + name + " 字段创建成功");
        deleteFieldsCache(appId, clazzName);
    }

    public void update(String appId, String clazzName, String fieldName, Field field) {
        Field exist = get(appId, clazzName, fieldName);
        exist.setSecurity(field.isSecurity());
        exist.setRequired(field.isRequired());
        exist.setDescription(field.getDescription());
        fieldRepository.save(exist);
        deleteFieldsCache(appId, clazzName);
        log.info("App:" + appId + " Class:" + clazzName + " Field:" + fieldName + " 字段已更新 " + jsonUtil.writeValueAsString(exist));
    }

    public void setRequired(String appId, String clazzName, String fieldName, boolean required) {
        Field exist = get(appId, clazzName, fieldName);
        exist.setRequired(required);
        fieldRepository.save(exist);
        deleteFieldsCache(appId, clazzName);
        log.info("App:" + appId + " Class:" + clazzName + " Field:" + fieldName + " 字段已更新 " + jsonUtil.writeValueAsString(exist));
    }

    public void setSecurity(String appId, String clazzName, String fieldName, boolean security) {
        Field exist = get(appId, clazzName, fieldName);
        exist.setSecurity(security);
        fieldRepository.save(exist);
        deleteFieldsCache(appId, clazzName);
        log.info("App:" + appId + " Class:" + clazzName + " Field:" + fieldName + " 字段已更新 " + jsonUtil.writeValueAsString(exist));
    }

    public void delete(String appId, String clazzName, String name) {
        Field field = get(appId, clazzName, name);
        if (field.isInternal()) {
            //内建字段禁止删除
            throw new SimpleError(SimpleCode.FIELD_INTERNAL);
        }
        //删除该字段所有数据
        objectService.deleteFieldData(appId, clazzName, name);
        log.debug("App:" + appId + " Class:" + clazzName + " Field:" + name + " 数据删除成功");
        deleteFieldsCache(appId, clazzName);
        //删除字段
        fieldRepository.delete(field);
        log.debug("App:" + appId + " Class:" + clazzName + " Field:" + name + " 已删除");
    }

    public long deleteAll(String appId, String clazzName) {
        Clazz clazz = clazzService.get(appId, clazzName);
        deleteFieldsCache(appId, clazzName);
        long count = fieldRepository.deleteByClazz(clazz);
        log.info("App:" + appId + " Class:" + clazzName + " 字段全部删除");
        return count;
    }

    public Field get(String appId, String clazzName, String name) {
        Clazz clazz = clazzService.get(appId, clazzName);
        Field field = fieldRepository.findByClazzAndName(clazz, name);
        if (field == null) {
            throw new SimpleError(SimpleCode.FIELD_NOT_FOUND);
        }
        return field;
    }

    public List<Field> list(String appId, String clazzName) {
        List<Field> fields = getFieldsCache(appId, clazzName);
        if (fields == null) {
            //未找到缓存
            Clazz clazz = clazzService.get(appId, clazzName);
            //排序
            Sort.Order o1 = new Sort.Order(Sort.Direction.DESC, "internal");
            Sort.Order o2 = new Sort.Order(Sort.Direction.ASC, "name");
            Sort sort = new Sort(o1, o2);
            PageRequest pager = new PageRequest(0, 1000, sort);
            fields = fieldRepository.findByClazzId(clazz.getId(), pager);
            setFieldsCache(appId, clazzName, fields);
        }
        return fields;
    }

    public List<FieldExport> export(String appId, String clazzName) {
        List<Field> fields = list(appId, clazzName);
        List<FieldExport> fieldExports = new LinkedList<>();
        for (Field field : fields) {
            FieldExport fieldExport = new FieldExport();
            BeanUtils.copyProperties(field, fieldExport);
            fieldExports.add(fieldExport);
        }
        return fieldExports;
    }

    public void importData(String appId, String clazzName, FieldExport fieldExport) {
        Field field = new Field();
        BeanUtils.copyProperties(fieldExport, field);
        insert(appId, clazzName, field);
    }

    public boolean isNameValid(String name) {
        String regex = "^[a-zA-Z][a-zA-Z0-9_]*$";
        return Pattern.matches(regex, name);
    }

    /**
     * 判断字段名称是否为保留字
     *
     * @param name 字段名称
     * @return 是否为保留字
     */
    public boolean isReserved(String name) {
        return name.equalsIgnoreCase("id") ||
                name.equalsIgnoreCase("createdAt") ||
                name.equalsIgnoreCase("updatedAt") ||
                name.equalsIgnoreCase("ACL");
    }

    private List<Field> getFieldsCache(String appId, String clazzName) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String string = ops.get("App_" + appId + "_Fields_" + clazzName);
        Field[] fields = jsonUtil.readValue(string, Field[].class);
        return fields == null ? null : Arrays.asList(fields);
    }

    private void setFieldsCache(String appId, String clazzName, List<Field> fields) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("App_" + appId + "_Fields_" + clazzName, jsonUtil.writeValueAsString(fields));
    }

    private void deleteFieldsCache(String appId, String clazzName) {
        redisTemplate.delete("App_" + appId + "_Fields_" + clazzName);
        log.debug("App:" + appId + " Class:" + clazzName + " 所有字段缓存已清除");
    }

}
