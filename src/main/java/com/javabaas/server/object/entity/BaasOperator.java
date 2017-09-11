package com.javabaas.server.object.entity;

/**
 * Created by Codi on 2017/8/4.
 */
public class BaasOperator {

    private BaasOperatorEnum type;
    private Object value;

    public BaasOperator(BaasOperatorEnum type, Object value) {
        this.type = type;
        this.value = value;
    }

    public BaasOperatorEnum getType() {
        return type;
    }

    public void setType(BaasOperatorEnum type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
