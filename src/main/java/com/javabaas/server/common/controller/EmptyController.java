package com.javabaas.server.common.controller;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleResult;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 无效请求地址
 * Created by Codi on 2017/7/3.
 */
@RestController
public class EmptyController implements ErrorController {

    private final static String ERROR_PATH = "/error";

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public SimpleResult error() {
        return SimpleResult.error(SimpleCode.NOT_FOUND);
    }


}
