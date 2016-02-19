package com.staryet.baas.object.entity.error;

import com.staryet.baas.common.entity.SimpleCode;
import com.staryet.baas.common.entity.SimpleError;

/**
 * 字段类型错误
 */
public class FieldTypeError extends SimpleError {

    public FieldTypeError(String message) {
        super(SimpleCode.OBJECT_FIELD_TYPE_ERROR.getCode(), message);
    }

}
