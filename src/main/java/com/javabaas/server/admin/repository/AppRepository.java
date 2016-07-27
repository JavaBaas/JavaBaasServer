package com.javabaas.server.admin.repository;

import com.javabaas.server.admin.entity.App;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Staryet on 15/6/5.
 */
public interface AppRepository extends MongoRepository<App, String> {

    long deleteByName(String name);

    App findByName(String name);

}
