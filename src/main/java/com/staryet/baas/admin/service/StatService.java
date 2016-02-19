package com.staryet.baas.admin.service;

import com.staryet.baas.admin.entity.ApiMethod;
import com.staryet.baas.admin.entity.ApiStat;
import com.staryet.baas.admin.entity.Clazz;
import com.staryet.baas.admin.entity.ClientPlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 统计相关
 * Created by Codi on 15/10/20.
 */
@Service
public class StatService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ClazzService clazzService;

    public long getYesterdayApiCount(String appId) {
        Calendar now = new GregorianCalendar();
        now.add(Calendar.DAY_OF_YEAR, -1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String yesterday = simpleDateFormat.format(now.getTime());
        return getDayApiCount(appId, yesterday);
    }

    public long getCurrentMonthApiCount(String appId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        String month = simpleDateFormat.format(new Date());
        return getMonthApiCount(appId, month);
    }

    /**
     * 查询某日请求总量
     * (只能用于查询昨天或以前的数据)
     *
     * @param appId 应用id
     * @param date  日期 yyyyMMdd
     * @return 请求总量
     */
    private long getDayApiCount(String appId, String date) {
        String key = "stat" + "_" + appId + "_" + date;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String result = ops.get(key);
        if (StringUtils.isEmpty(result)) {
            Long count = get(appId, null, null, null, date);
            ops.set(key, String.valueOf(count));
            return count;
        } else {
            return Long.valueOf(result);
        }
    }

    /**
     * 查询月请求总量
     *
     * @param appId 应用id
     * @param month 日期yyyyMM
     * @return 请求总量
     */
    private long getMonthApiCount(String appId, String month) {
        long sum = 0;
        List<String> dates = getDates(month + "01", month + "31");
        for (String date : dates) {
            long count = getDayApiCount(appId, date);
            sum += count;
        }
        return sum;
    }

    public void add(ApiStat apiStat) {
        if (apiStat != null && apiStat.getPlat() != null) {
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            ops.increment(apiStat.toString(), 1);
        }
    }

    public Long get(ApiStat apiStat) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String value = ops.get(apiStat.toString());
        return StringUtils.isEmpty(value) ? 0 : Long.valueOf(value);
    }

    public List<Long> get(String appId, ApiMethod method, String clazz, ClientPlatform plat, String from, String to) {
        List<String> dates = getDates(from, to);
        List<Long> result = new ArrayList<>();
        ApiMethod[] apiMethods = null;
        if (method != null) {
            apiMethods = new ApiMethod[1];
            apiMethods[0] = method;
        }
        List<String> clazzs = null;
        if (clazz != null) {
            clazzs = new ArrayList<>();
            clazzs.add(clazz);
        }
        ClientPlatform[] plats = null;
        if (plat != null) {
            plats = new ClientPlatform[1];
            plats[0] = plat;
        }
        for (String date : dates) {
            result.add(get(appId, apiMethods, clazzs, plats, date));
        }
        return result;
    }

    /**
     * 获取数据总和
     */
    private Long get(String appId, ApiMethod[] apiMethods, List<String> clazzs, ClientPlatform[] plats, String date) {
        long sum = 0;
        if (apiMethods == null) {
            apiMethods = ApiMethod.values();
        }
        if (clazzs == null) {
            List<Clazz> clazzList = clazzService.list(appId);
            clazzs = new ArrayList<>();
            for (Clazz clazz : clazzList) {
                clazzs.add(clazz.getName());
            }
        }
        if (plats == null) {
            plats = ClientPlatform.values();
        }
        for (ApiMethod method : apiMethods) {
            for (String clazz : clazzs) {
                for (ClientPlatform plat : plats) {
                    sum += get(new ApiStat(appId, plat.toString(), clazz, method, date));
                }
            }
        }
        return sum;
    }

    private List<String> getDates(String from, String to) {
        Integer start = Integer.valueOf(from);
        Integer end = Integer.valueOf(to);
        List<String> dates = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            dates.add(String.valueOf(i));
        }
        return dates;
    }

}
