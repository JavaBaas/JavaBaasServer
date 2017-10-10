package com.javabaas.server.admin.controller;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.dto.AppDto;
import com.javabaas.server.admin.entity.dto.AppExport;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.StatService;
import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.object.service.ObjectService;
import com.javabaas.server.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 应用控制器
 * Created by Staryet on 15/9/17.
 */
@RestController
@RequestMapping(value = "/api/admin/app")
public class AppController {

    @Autowired
    private AppService appService;
    @Autowired
    private StatService statService;
    @Autowired
    private ObjectService objectService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public SimpleResult insert(@RequestBody App app) {
        App newApp = appService.insert(app);
        SimpleResult result = SimpleResult.success();
        result.putData("result", newApp);
        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public SimpleResult delete(@PathVariable String id) {
        appService.delete(id);
        return SimpleResult.success();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult get(@PathVariable String id) {
        App app = appService.get(id);
        SimpleResult result = SimpleResult.success();
        result.putData("result", app);
        return result;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult  list() {
        List<App> apps = appService.list();
        LinkedList<AppDto> appDTOs = new LinkedList<>();
        apps.forEach(app -> {
            AppDto dto = new AppDto();
            BeanUtils.copyProperties(app, dto);
            appDTOs.add(dto);
            //计算用户总数
            dto.setUserCount(objectService.count(app.getId(), UserService.USER_CLASS_NAME, null, null, true));
            //请求数总和
            dto.setYesterday(statService.getYesterdayApiCount(app.getId()));
            dto.setCurrentMonth(statService.getCurrentMonthApiCount(app.getId()));
        });
        SimpleResult result = SimpleResult.success();
        result.putData("result", appDTOs);
        return result;
    }

    @RequestMapping(value = "/{id}/resetKey", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult resetKey(@PathVariable String id) {
        appService.resetKey(id);
        return SimpleResult.success();
    }

    @RequestMapping(value = "/{id}/resetMasterKey", method = RequestMethod.PUT)
    @ResponseBody
    public SimpleResult resetMasterKey(@PathVariable String id) {
        appService.resetMasterKey(id);
        return SimpleResult.success();
    }

    @RequestMapping(value = "/{id}/export", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult export(@PathVariable String id) {
        AppExport appExport = appService.export(id);
        SimpleResult result = SimpleResult.success();
        result.putData("result", appExport);
        return result;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult importData(@RequestBody AppExport appExport) {
        appService.importData(appExport);
        return SimpleResult.success();
    }

}