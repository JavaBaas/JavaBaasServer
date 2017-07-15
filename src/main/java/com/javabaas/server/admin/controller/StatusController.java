package com.javabaas.server.admin.controller;

import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.object.dao.impl.mongo.MongoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Staryet on 15/8/11.
 */
@RestController
@RequestMapping(value = "/api/admin/status")
public class StatusController {

    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private FieldService fieldService;

    @Autowired
    private MongoDao dao;

    @RequestMapping(value = "/redis", method = RequestMethod.GET)
    public String redis() {
//        String f1 = templates.opsForValue().get("f1");
//        List<Field> message = JSON.parseArray(f1, Field.class);
        return "ok";
    }

    @RequestMapping(value = "/mongo", method = RequestMethod.GET)
    public String mongo() {
//        List<Field> fields = fieldService.find("test");
        return "ok";
    }

}
