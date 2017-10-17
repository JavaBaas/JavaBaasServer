package com.javabaas.server.common.listener;

import com.javabaas.server.common.service.TimeService;
import com.javabaas.server.config.AuthConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 监听系统启动成功
 * Created by Codi on 15/10/30.
 */
@Component
public class ApplicationEventListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    public static boolean error;
    private static boolean ready;
    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private TimeService timeService;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent applicationReadyEvent) {
        //启动端口
        int port = applicationReadyEvent.getEmbeddedServletContainer().getPort();
        //记录启动时间
        timeService.setStartedTime(new Date());
        ready = true;
        //check out database health
        try {
            RedisConnection connection = RedisConnectionUtils.getConnection(this.redisConnectionFactory);
            try {
                connection.info();
            } catch (Exception e) {
                error = true;
                log.error(e, e);
            } finally {
                RedisConnectionUtils.releaseConnection(connection, this.redisConnectionFactory);
            }
        } catch (Exception e) {
            error = true;
            log.error(e, e);
            log.error("Redis error!");
        }
        try {
            this.mongoTemplate.executeCommand("{ buildInfo: 1 }");
        } catch (Exception e) {
            error = true;
            log.error(e, e);
            log.error("MongoDB error!");
        }
        if (!error) {
            //应用启动成功
            success(port);
        } else {
            //应用启动失败
            log.error("JavaBaasServer failed to start!");
        }
    }

    private void success(int port) {
        //显示配置信息
        log.info("JavaBaasServer started.");
        log.info("Key: " + authConfig.getAdminKey());
        log.info("Timeout: " + authConfig.getTimeout());
        //显示浏览器
        log.info("JavaBaas status at " + getLocalHost() + ":" + port);
        log.info("Browse REST API at " + getLocalHost() + ":" + port + "/explorer.html");
    }

    private String getLocalHost() {
        return "http://localhost";
    }

    public static boolean isReady() {
        return ready;
    }
}
