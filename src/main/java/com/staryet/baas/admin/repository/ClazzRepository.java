package com.staryet.baas.admin.repository;

import com.staryet.baas.admin.entity.Clazz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Staryet on 15/6/5.
 */
public interface ClazzRepository extends MongoRepository<Clazz, String> {

    long deleteByName(String name);

    Clazz findByAppIdAndName(String appId, String name);

    List<Clazz> findByAppId(String appId, Pageable pageable);

    long deleteByAppId(String appId);

}
