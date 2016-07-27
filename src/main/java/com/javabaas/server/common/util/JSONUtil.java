package com.javabaas.server.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Json处理器
 * Created by Staryet on 15/9/28.
 */
@Component
public class JSONUtil {

    @Autowired
    private ObjectMapper objectMapper;

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
