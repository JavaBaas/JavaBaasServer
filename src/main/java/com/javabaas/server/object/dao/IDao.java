package com.javabaas.server.object.dao;

import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.object.entity.BaasSort;
import com.javabaas.server.object.entity.BaasObject;

import java.util.List;

/**
 * 对象存储抽象
 * Created by Codi on 15/12/9.
 */
public interface IDao {

    BaasObject insert(String appId, String className, BaasObject object);

    BaasObject findOne(String appId, String className, BaasQuery query);

    List<BaasObject> find(String appId, String className, BaasQuery query, BaasSort sort, Integer limit, Integer skip);

    void update(String appId, String className, BaasQuery query, BaasObject object);

    void increment(String appId, String className, BaasQuery query, BaasObject object);

    void remove(String appId, String className, BaasQuery query);

    void removeApp(String appId);

    void removeClass(String appId, String className);

    void removeField(String appId, String className, String fieldName);

    long count(String appId, String className, BaasQuery query);

}
