package com.javabaas.server.object.util;

import com.javabaas.server.admin.entity.FieldType;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.object.entity.BaasList;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasOperator;
import com.javabaas.server.object.entity.BaasOperatorEnum;
import com.javabaas.server.object.entity.error.OperatorWrongTypeError;
import com.javabaas.server.object.entity.error.OperatorWrongValueError;

/**
 * Created by Codi on 2017/8/5.
 */
public class BaasOperatorUtil {

    /**
     * 获取字段处理器
     *
     * @param fieldType 字段类型
     * @param value     处理器
     */
    public static BaasOperator getOperator(String key, int fieldType, Object value) {
        if (value instanceof BaasOperator) {
            //传入的类型就是BaasOperator 只需检查BaasOperator有效性
            BaasOperator operator = (BaasOperator) value;
            checkOperator(key, fieldType, operator);
            return operator;
        }
        //传入类型非BaasOperator 公提取value中提取BaasOperator
        if (!(value instanceof BaasObject) || ((BaasObject) value).get("__op") == null) {
            //非操作指令
            return null;
        } else {
            //操作指令
            BaasObject baasObject = (BaasObject) value;
            return extractOperator(key, fieldType, baasObject);
        }
    }

    private static BaasOperator extractOperator(String key, int fieldType, BaasObject baasObject) {
        String type = (String) baasObject.get("__op");
        BaasOperatorEnum operatorType = BaasOperatorEnum.getOperator(type);
        if (operatorType == null) {
            //操作符不存在
            SimpleError.e(SimpleCode.OBJECT_OPERATOR_NOT_EXIST);
            return null;
        } else {
            Object operatorValue = null;
            //判断操作符值及类型是否正确
            switch (operatorType) {
                case DELETE:
                    operatorValue = null;
                    break;
                case INCREMENT:
                case MULTIPLY:
                    operatorValue = baasObject.get("amount");
                    if (operatorValue == null || !(operatorValue instanceof Integer)) {
                        throw new OperatorWrongValueError(key, operatorValue);
                    }
                    if (fieldType != FieldType.NUMBER) {
                        throw new OperatorWrongTypeError(key, operatorType.getName());
                    }
                    break;
                case ADD:
                case ADD_UNIQUE:
                case REMOVE:
                    operatorValue = baasObject.get("objects");
                    if (operatorValue == null || !(operatorValue instanceof BaasList)) {
                        throw new OperatorWrongValueError(key, operatorValue);
                    }
                    if (fieldType != FieldType.ARRAY) {
                        throw new OperatorWrongTypeError(key, operatorType.getName());
                    }
                    break;
            }
            return new BaasOperator(operatorType, operatorValue);
        }
    }

    private static void checkOperator(String key, int fieldType, BaasOperator baasOperator) {
        BaasOperatorEnum operatorType = baasOperator.getType();
        Object operatorValue = baasOperator.getValue();
        if (operatorType == null) {
            //操作符不存在
            SimpleError.e(SimpleCode.OBJECT_OPERATOR_NOT_EXIST);
        } else {
            //判断操作符值及类型是否正确
            switch (operatorType) {
                case INCREMENT:
                case MULTIPLY:
                    if (operatorValue == null || !(operatorValue instanceof Integer)) {
                        throw new OperatorWrongValueError(key, operatorValue);
                    }
                    if (fieldType != FieldType.NUMBER) {
                        throw new OperatorWrongTypeError(key, operatorType.getName());
                    }
                    break;
                case ADD:
                case ADD_UNIQUE:
                case REMOVE:
                    if (operatorValue == null || !(operatorValue instanceof BaasList)) {
                        throw new OperatorWrongValueError(key, operatorValue);
                    }
                    if (fieldType != FieldType.ARRAY) {
                        throw new OperatorWrongTypeError(key, operatorType.getName());
                    }
                    break;
            }
        }
    }

}
