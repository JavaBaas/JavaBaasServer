package com.javabaas.server.object.entity.error;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;

/**
 * Created by Codi on 2018/12/5.
 */
public class ObjectInsertError extends SimpleError {

    public ObjectInsertError() {
        super(SimpleCode.OBJECT_INSERT_ERROR);
    }
}
