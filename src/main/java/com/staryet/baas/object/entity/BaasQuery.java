package com.staryet.baas.object.entity;

import java.util.Map;

/**
 * Created by Staryet on 15/6/25.
 */
public class BaasQuery extends BaasObject {

    public BaasQuery() {
    }

    public BaasQuery(Map<String, Object> m) {
        super(m);
    }

    public BaasQuery(String key, Object value) {
        super(key, value);
    }
}
