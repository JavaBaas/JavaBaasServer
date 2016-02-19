package com.staryet.baas.admin.controller;

import com.staryet.baas.admin.entity.Field;
import com.staryet.baas.admin.service.FieldService;
import com.staryet.baas.common.entity.SimpleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.staryet.baas.common.entity.SimpleResult.success;

/**
 * Created by Staryet on 15/6/15.
 */
@RestController
@RequestMapping(value = "/api/master/clazz")
public class FieldController {

    @Autowired
    private FieldService fieldService;

    @RequestMapping(value = "/{clazzName}/field", method = RequestMethod.POST)
    public SimpleResult insert(@RequestHeader(value = "JB-AppId") String appId,
                               @PathVariable String clazzName,
                               @RequestBody Field field) {
        fieldService.insert(appId, clazzName, field);
        return success();
    }

    @RequestMapping(value = "/{clazzName}/field/{name}", method = RequestMethod.DELETE)
    public SimpleResult delete(@RequestHeader(value = "JB-AppId") String appId,
                               @PathVariable String clazzName,
                               @PathVariable String name) {
        fieldService.delete(appId, clazzName, name);
        return success();
    }

    @RequestMapping(value = "/{clazzName}/field/{name}", method = RequestMethod.PUT)
    public SimpleResult update(@RequestHeader(value = "JB-AppId") String appId,
                               @PathVariable String clazzName,
                               @PathVariable String name,
                               @RequestBody Field field) {
        fieldService.update(appId, clazzName, name, field);
        return success();
    }

    @RequestMapping(value = "/{clazzName}/field/{name}", method = RequestMethod.GET)
    public Field get(@RequestHeader(value = "JB-AppId") String appId,
                     @PathVariable String clazzName,
                     @PathVariable String name) {
        return fieldService.get(appId, clazzName, name);
    }

    @RequestMapping(value = "/{clazzName}/field", method = RequestMethod.GET)
    public List<Field> list(@RequestHeader(value = "JB-AppId") String appId,
                            @PathVariable String clazzName) {
        return fieldService.list(appId, clazzName);
    }

}
