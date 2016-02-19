package com.staryet.baas.log.controller;

import com.staryet.baas.log.entity.BaasLog;
import com.staryet.baas.log.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日志
 * Created by Codi on 15/10/31.
 */
@RestController
@RequestMapping(value = "/api/admin/log")
public class LogController {

    @Autowired
    private LogService logService;

    @RequestMapping(value = "/{serverName}", method = RequestMethod.GET)
    public List<BaasLog> getLog(@PathVariable("serverName") String serverName,
                                @RequestParam(required = false) String level,
                                @RequestParam(required = false) String logger,
                                @RequestParam(required = false) Long from,
                                @RequestParam(required = false) Long to) {
        return logService.getLogs(serverName, level, logger, from, to);
    }

}
