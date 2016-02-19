package com.staryet.baas.object.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Staryet on 15/8/3.
 */
public class BaasInclude {

    private String name;
    private Map<String, BaasInclude> sub;

    public BaasInclude(String name) {
        this.name = name;
        sub = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, BaasInclude> getSubs() {
        return sub;
    }

    public BaasInclude getSub(String name) {
        return sub.get(name);
    }

    public void addSub(BaasInclude include) {
        sub.put(include.getName(), include);
    }

}
