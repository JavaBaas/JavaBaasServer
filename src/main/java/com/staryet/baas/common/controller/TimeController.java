package com.staryet.baas.common.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 获取服务器时间
 * Created by Codi on 15/10/21.
 */
@RestController
@RequestMapping(value = "/time")
public class TimeController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    public long time()  {
        return new Date().getTime();
    }

}
