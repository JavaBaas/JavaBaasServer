package com.javabaas.server.common.service;

import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by Codi on 2017/10/11.
 */
@Service
public class TimeService {

    private Date startedTime;

    public Date getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(Date startedTime) {
        this.startedTime = startedTime;
    }
}
