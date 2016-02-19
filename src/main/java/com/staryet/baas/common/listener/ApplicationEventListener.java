package com.staryet.baas.common.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 监听系统启动成功
 * Created by Codi on 15/10/30.
 */
@Component
public class ApplicationEventListener implements ApplicationListener<ApplicationReadyEvent> {

    public static boolean ready;
    private Log log = LogFactory.getLog(getClass());

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        //系统启动成功
        ready = true;
        log.info("JavaBaas 系统启动成功");
    }

    public static boolean isReady() {
        return ready;
    }
}
