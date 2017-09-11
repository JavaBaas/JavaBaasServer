package com.javabaas.server.object.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Staryet on 15/6/23.
 */
public class BaasList extends ArrayList<Object> {

    public BaasList() {
    }

    public BaasList(Collection<?> c) {
        super(c);
    }

    @SuppressWarnings("unchecked")
    public BaasObject getBaasObject(int index) {
        Object value = get(index);
        Map<String, Object> object = null;
        try {
            object = (Map<String, Object>) value;
        } catch (ClassCastException ignored) {
        }
        return object == null ? null : new BaasObject(object);
    }

}
