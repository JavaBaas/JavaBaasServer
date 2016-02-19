package com.staryet.baas.admin.repository;

import com.staryet.baas.admin.entity.Clazz;
import com.staryet.baas.admin.entity.Field;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Staryet on 15/6/5.
 */
public interface FieldRepository extends MongoRepository<Field, String> {

    List<Field> findByClazzId(String id, Pageable pageable);

    long deleteByClazzAndName(Clazz clazz, String name);

    Field findByClazzAndName(Clazz clazz, String name);

    long deleteByClazz(Clazz clazz);
}
