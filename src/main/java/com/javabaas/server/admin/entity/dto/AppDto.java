package com.javabaas.server.admin.entity.dto;

import com.javabaas.server.admin.entity.App;

/**
 * Created by Codi on 16/1/4.
 */
public class AppDto extends App {

    private long userCount;
    private long yesterday;
    private long currentMonth;

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getYesterday() {
        return yesterday;
    }

    public void setYesterday(long yesterday) {
        this.yesterday = yesterday;
    }

    public long getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(long currentMonth) {
        this.currentMonth = currentMonth;
    }
}
