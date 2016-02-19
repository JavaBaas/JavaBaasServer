package com.staryet.baas.object.entity.error;

import com.staryet.baas.common.entity.SimpleCode;
import com.staryet.baas.common.entity.SimpleError;

/**
 * 字段类型错误
 */
public class DuplicateKeyError extends SimpleError {

    public DuplicateKeyError(String key) {
        super(SimpleCode.OBJECT_DUPLICATE_KEY.getCode(), "唯一字段" + key + "禁止重复值");
    }

}
