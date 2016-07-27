package com.javabaas.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by Codi on 16/1/5.
 */
public class Test {


    private static ObjectMapper objectMapper;

    private Map root;

    public static void main(String... strings) throws IOException {
        new Test().test();

    }

    public void test() throws IOException {
        System.out.println("aaa".split(";")[0]);
//        objectMapper = new ObjectMapper();
//        root = objectMapper.readValue("{\"c\":{\"$sub\":{\"where\":{\"d\":{\"$sub\":{\"where\":{\"name\":\"d\"},\"searchClass\":\"D\"}},\"e\":{\"$sub\":{\"where\":{\"name\":\"e\"},\"searchClass\":\"E\"}}},\"searchClass\":\"C\"}}}", Map.class);
//        handleSub("", null, root);
    }

    private boolean handleSub(String field, Map<String, Object> parent, Map<String, Object> query) {
        boolean flag = false;
        boolean sub = false;
        Set<Map.Entry<String, Object>> entries = query.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals("$sub")) {
                flag = true;
            }
            if (entry.getValue() instanceof Map) {
                sub = handleSub(key, query, (Map<String, Object>) value) || sub;
            }
        }
        //子节点循环完毕
        //返回后判断无子节点且节点名称为$sub则进行子查询处理
        if (field.equals("$sub") && !sub) {
            replaceSub(parent, query);
            System.out.println(field);
            try {
                System.out.println(objectMapper.writeValueAsString(root));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    private void replaceSub(Map<String, Object> parent, Map<String, Object> sub) {
        System.out.println("replace");
        parent.remove("$sub");
        parent.put("$in", "{}");
    }

}
