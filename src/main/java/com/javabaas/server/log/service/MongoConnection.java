package com.javabaas.server.log.service;

import com.mongodb.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Codi on 15/10/31.
 */
public class MongoConnection {

    private static MongoClient mongo;
    private static DB db;

    public static void connectToMongo(String database, String username, String password, String host, int port) {
        if (db == null) {
            MongoClientOptions options = MongoClientOptions.builder().build();
            List<MongoCredential> credentials = Arrays.asList(MongoCredential.createScramSha1Credential(
                    username, "admin", password.toCharArray()));
            mongo = new MongoClient(Arrays.asList(new ServerAddress(host, port)),
                    credentials, options);
            db = mongo.getDB(database);
        }
    }

    public static DB getDb() {
        return db;
    }

    public static DBCollection getLogCollection() {
        return db.getCollection("log");
    }

    public static void close() {
        if (mongo != null) {
            mongo.close();
        }
    }
}
