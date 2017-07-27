package com.javabaas.server.cloud.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供内部运行时环境所需要的所有工具
 * Created by Codi on 2017/7/24.
 */
@Service
public class CloudEngineManager {

    @Autowired
    private JB jb;

    private Map<String, ScriptEngine> engineMap = new HashMap<>();

    public ScriptEngine getCloudEngine(String appId, String name) throws ScriptException {
        ScriptEngine engine = engineMap.get(getKey(appId, name));
        if (engine == null) {
            ScriptEngineManager manager = new ScriptEngineManager();
            engine = manager.getEngineByName("nashorn");
            engine.put("JB", jb);

            engine.eval("function cloud(request){\n" +
                    "\tvar book = JB.get(\"Book\",\"b4f4290920174c058307fd502fedea3a\");\n" +
                    "\tprint(book);\n" +
                    "\tJB.log(book);\n" +
                    "}");

            engineMap.put(getKey(appId, name), engine);
        }
        return engine;
    }

    private String getKey(String appId, String name) {
        return appId + "_" + name;
    }

}
