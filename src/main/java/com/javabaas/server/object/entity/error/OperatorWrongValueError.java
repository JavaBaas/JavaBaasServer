package com.javabaas.server.object.entity.error;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;

/**
 * 字段类型错误
 */
public class OperatorWrongValueError extends SimpleError {

    public OperatorWrongValueError(String key, Object value) {
        super(SimpleCode.OBJECT_OPERATOR_WRONG_VALUE.getCode(),
                "字段: " + key + " 类型错误。类型 " + value.getClass() + " 不可用。");
    }

}
