package com.javabaas.server.object.service;

import com.javabaas.server.admin.entity.Field;
import com.javabaas.server.object.entity.BaasAcl;
import com.javabaas.server.object.entity.BaasObject;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Codi on 2017/9/15.
 */
@Component
public class ObjectExtractor {

    /**
     * 提取对象
     * 按照权限提取对象 无管理权限时过滤掉保密字段
     *
     * @param fields   对象字段列表
     * @param object   原对象
     * @param isMaster 是否为管理权限
     * @return 提取后对象
     */
    public BaasObject extractObject(List<Field> fields, BaasObject object, boolean isMaster) {
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

}
