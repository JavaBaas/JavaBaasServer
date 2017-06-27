package com.javabaas.server.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.javabaas.server.common.listener.ApplicationEventListener;
import com.javabaas.server.log.service.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

/**
 * MongoDb日志记录器
 * Created by Codi on 15/10/29.
 */
public class LogbackMongoAppender extends AppenderBase<ILoggingEvent> {

    private String server;
    private String username;
    private String database;
    private String password;
    private String host;
    private int port;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        MongoConnection.connectToMongo(database, username, password, host, port);
        if (ApplicationEventListener.isReady()) {
            //系统启动成功后 开始记录日志
            DBCollection c = MongoConnection.getDb().getCollection("log");
            BasicDBObject dbo = new BasicDBObject();
            dbo.append("server", server);
            dbo.put("level", iLoggingEvent.getLevel().toString());
            dbo.append("thread", iLoggingEvent.getThreadName());
            dbo.append("logger", iLoggingEvent.getLoggerName());
            dbo.append("message", iLoggingEvent.getFormattedMessage());
            dbo.put("timestamp", iLoggingEvent.getTimeStamp());
            c.insert(dbo);
        }
    }

    @Override
    public void stop() {
        MongoConnection.close();
        super.stop();
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
