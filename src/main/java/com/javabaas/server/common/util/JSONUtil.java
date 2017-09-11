package com.javabaas.server.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Json处理器
 * Created by Staryet on 15/9/28.
 */
@Component
public class JSONUtil {

    @Autowired
    private ObjectMapper objectMapper;
    @Resource(name = "baasMapper")
    private ObjectMapper baasMapper;

    public <T> T readValue(String content, Class<T> valueType) {
        if (content == null) {
            return null;
        }
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new SimpleError(SimpleCode.INTERNAL_JSON_ERROR);
        }
    }

    /**
     * 读取JavaBaas对象
     * 自动将Map反序列化为BaasObject
     * 自动将List反序列化为BaasList
     *
     * @param content 对象内容
     * @return BaasObject
     */
    public <T> T readBaas(String content, Class<T> valueType) {
        if (content == null) {
            return null;
        }
        try {
            return baasMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new SimpleError(SimpleCode.INTERNAL_JSON_ERROR);
        }
    }

    public <T> T readValue(String content, TypeReference valueType) {
        if (content == null) {
            return null;
        }
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new SimpleError(SimpleCode.INTERNAL_JSON_ERROR);
        }
    }

    public String writeValueAsString(Object value) {
        if (value != null) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new SimpleError(SimpleCode.INTERNAL_JSON_ERROR);
            }
        } else {
            return "";
        }
    }
}
