package com.javabaas.server.admin.controller;

import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.entity.ClazzAcl;
import com.javabaas.server.admin.entity.dto.ClazzExport;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.common.entity.SimpleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 类控制器
 * Created by Staryet on 15/6/15.
 */
@RestController
@RequestMapping(value = "/api/master/clazz")
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public SimpleResult insert(@RequestHeader(value = "JB-AppId") String appId, @RequestBody Clazz clazz) {
        clazzService.insert(appId, clazz);
        return SimpleResult.success();
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
    @ResponseBody
    public SimpleResult delete(@RequestHeader(value = "JB-AppId") String appId, @PathVariable String name) {
        clazzService.delete(appId, name);
        return SimpleResult.success();
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult get(@RequestHeader(value = "JB-AppId") String appId, @PathVariable String name) {
        Clazz clazz = clazzService.get(appId, name);
        SimpleResult result = SimpleResult.success();
        result.putData("result", clazz);
        return result;
    }

    @RequestMapping(value = "/{name}/export", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult export(@RequestHeader(value = "JB-AppId") String appId, @PathVariable String name) {
        ClazzExport clazzExport = clazzService.export(appId, name);
        SimpleResult result = SimpleResult.success();
        result.putData("result", clazzExport);
        return result;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult importData(@RequestHeader(value = "JB-AppId") String appId, @RequestBody ClazzExport clazzExport) {
        clazzService.importData(appId, clazzExport);
        return SimpleResult.success();
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult list(@RequestHeader(value = "JB-AppId") String appId) {
        List<Clazz> list = clazzService.list(appId);
        SimpleResult result = SimpleResult.success();
        result.putData("result", list);
        return result;
    }

    @RequestMapping(value = "/{name}/acl", method = RequestMethod.POST)
    @ResponseBody
    public SimpleResult setAcl(@RequestHeader(value = "JB-AppId") String appId, @PathVariable String name, @RequestBody ClazzAcl acl) {
        clazzService.setAcl(appId, name, acl);
        return SimpleResult.success();
    }

}
