package com.javabaas.server.config.repository;

import com.javabaas.server.config.entity.AppConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Codi on 2017/7/8.
 */
public interface AppConfigRepository extends MongoRepository<AppConfig, String> {

    AppConfig findByAppId(String appId);

    long deleteByAppId(String appId);

}
