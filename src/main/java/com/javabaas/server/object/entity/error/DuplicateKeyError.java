package com.javabaas.server.object.entity.error;

import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleCode;

/**
 * 字段类型错误
 */
public class DuplicateKeyError extends SimpleError {

    public DuplicateKeyError(String key) {
        super(SimpleCode.OBJECT_DUPLICATE_KEY.getCode(), "唯一字段" + key + "禁止重复值");
    }

}
