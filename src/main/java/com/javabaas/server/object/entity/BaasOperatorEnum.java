package com.javabaas.server.object.entity;

import org.springframework.util.StringUtils;

/**
 * Created by Codi on 2017/8/4.
 */
public enum BaasOperatorEnum {
    DELETE("Delete"),
    ADD("Add"),
    ADD_UNIQUE("AddUnique"),
    REMOVE("Remove"),
    INCREMENT("Increment"),
    MULTIPLY("Multiply");

    private String name;

    public String getName() {
        return name;
    }

    BaasOperatorEnum(String name) {
        this.name = name;
    }

    public static BaasOperatorEnum getOperator(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        } else {
            for (BaasOperatorEnum operator : BaasOperatorEnum.values()) {
                if (key.equals(operator.getName())) {
                    return operator;
                }
            }
        }
        return null;
    }
}
