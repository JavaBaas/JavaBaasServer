package com.javabaas.server.object.entity.error;

import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleCode;

/**
 * 字段类型错误
 */
public class FieldRequiredError extends SimpleError {

    public FieldRequiredError(String fieldName) {
        super(SimpleCode.OBJECT_FIELD_REQUIRED.getCode(), "字段" + fieldName + "不能为空!");
    }

}
