package com.javabaas.server.log.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.javabaas.server.log.entity.BaasLog;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Codi on 15/10/31.
 */
@Service
public class LogService {

    public List<BaasLog> getLogs(String serverName, String level, String logger, Long from, Long to) {
        DBCollection c = MongoConnection.getLogCollection();
        BasicDBObject query = new BasicDBObject();
        query.put("server", serverName);
        if (level != null) {
            query.put("level", level);
        }
        if (logger != null) {
            query.put("logger", logger);
        }
        if (from != null) {
            query.put("timestamp", new BasicDBObject("$gt", from));
        }
        if (to != null) {
            query.put("timestamp", new BasicDBObject("$lt", to));
        }
        BasicDBObject sort = new BasicDBObject();
        //默认排序为时间倒序
        sort.put("timestamp", -1);
        LinkedList<BaasLog> result = new LinkedList<>();
        DBCursor cursor = c.find(query).sort(sort).limit(1000);
        cursor.forEach(dbo -> {
            dbo.removeField("_id");
            result.add(new BaasLog((BasicDBObject) dbo));
        });
        return result;
    }

}
