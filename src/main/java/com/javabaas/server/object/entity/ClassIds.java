package com.javabaas.server.object.entity;

import java.util.*;

/**
 * Created by Staryet on 15/8/5.
 */
public class ClassIds {

    Map<String, List<String>> classes = new HashMap<>();

    public void addId(String className, String id) {
        List<String> classIds = getIds(className);
        classIds.add(id);
    }

    public List<String> getIds(String className) {
        List<String> classIds = classes.get(className);
        if (classIds == null) {
            classIds = new ArrayList<>();
            classes.put(className, classIds);
        }
        return classIds;
    }

    public Set<String> getClassNames() {
        return classes.keySet();
    }

}
