package com.staryet.baas.common.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.staryet.baas.common.entity.SimpleCode;
import com.staryet.baas.common.entity.SimpleError;
import com.staryet.baas.common.entity.SimpleResult;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * Created by Staryet on 15/6/17.
 */
@ControllerAdvice
public class ErrorController {

    private Log log = LogFactory.getLog(getClass());

    @ExceptionHandler(SimpleError.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SimpleResult errorResponse(SimpleError e) {
        return SimpleResult.fromError(e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SimpleResult errorResponse(HttpMessageNotReadableException t) {
        return new SimpleResult(SimpleCode.REQUEST_PARAM_ERROR);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SimpleResult errorResponse(MissingServletRequestParameterException t) {
        return new SimpleResult(SimpleCode.REQUEST_PARAM_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SimpleResult errorResponse(MethodArgumentNotValidException t) {
        //数据校验失败
        SimpleResult result = new SimpleResult(SimpleCode.REQUEST_PARAM_ERROR);
        //将结果转换为可读性更好的格式
        StringBuilder builder = new StringBuilder();
        List<ObjectError> errors = t.getBindingResult().getAllErrors();
        for (ObjectError error : errors) {
            builder.append(error.getDefaultMessage()).append(" ");
        }
        result.setMessage(builder.toString());
        return result;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SimpleResult errorResponse(HttpRequestMethodNotSupportedException t) {
        return new SimpleResult(SimpleCode.REQUEST_METHOD_ERROR);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SimpleResult errorResponse(HttpMediaTypeNotSupportedException t) {
        return new SimpleResult(SimpleCode.REQUEST_CONTENT_TYPE_ERROR);
    }

    @ExceptionHandler(JsonParseException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SimpleResult errorResponse(JsonParseException t) {
        return new SimpleResult(SimpleCode.REQUEST_JSON_ERROR);
    }

    @ExceptionHandler(ClientAbortException.class)
    public void errorResponse(ClientAbortException t) {
        //不处理此异常
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SimpleResult errorResponse(Throwable t) {
        log.error(t, t);
        return new SimpleResult(SimpleCode.INTERNAL_ERROR);
    }

}
