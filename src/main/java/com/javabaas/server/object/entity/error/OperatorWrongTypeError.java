package com.javabaas.server.object.entity.error;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;

/**
 * 字段类型错误
 */
public class OperatorWrongTypeError extends SimpleError {

    public OperatorWrongTypeError(String key, String name) {
        super(SimpleCode.OBJECT_OPERATOR_WRONG_TYPE.getCode(),
                "字段: " + key + " 操作符类型错误。操作符 " + name + " 不可用。");
    }


}
