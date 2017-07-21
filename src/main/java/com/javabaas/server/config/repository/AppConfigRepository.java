package com.javabaas.server.config.repository;

import com.javabaas.server.config.entity.AppConfigs;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Codi on 2017/7/8.
 */
public interface AppConfigRepository extends MongoRepository<AppConfigs, String> {

    AppConfigs findByAppId(String appId);

    long deleteByAppId(String appId);

}
