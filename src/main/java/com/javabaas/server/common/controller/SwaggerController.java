package com.javabaas.server.common.controller;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.entity.Field;
import com.javabaas.server.admin.entity.FieldType;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.admin.service.FieldService;
import com.javabaas.server.common.util.JSONUtil;
import io.swagger.models.*;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.properties.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static com.javabaas.server.admin.util.swagger.ParameterUtil.*;

/**
 * Created by Codi on 2017/10/6.
 */
@RestController
@RequestMapping(value = "/swagger")
public class SwaggerController {
    private Log log = LogFactory.getLog(getClass());
    @Autowired
    private AppService appService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    JSONUtil mapper;


    /**
     * 获取指定名称app的swagger数据
     *
     * @param name 应用名
     * @return swagger.json
     */
    @RequestMapping(value = "/{name}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Swagger app(HttpServletRequest request, @PathVariable String name) {
        //获取app数据
        App app = appService.getAppByName(name);
        //创建swagger
        Swagger swagger = initSwagger(app, request);
        //获取类数据
        List<Clazz> clazzs = clazzService.list(app.getId());
        //添加类模型
        addClazzModels(app, clazzs, swagger);
        //构建类方法
        addClazzPath(clazzs, swagger);
        //返回swagger数据
        return swagger;
    }

    private Swagger initSwagger(App app, HttpServletRequest request) {
        Swagger swagger = new Swagger();
        //生成本地地址
        StringBuffer url = request.getRequestURL();
        String uri = request.getRequestURI();
        String host = url.substring(0, url.indexOf(uri));
        host = host.replace("http://", "");
        //设置host
        swagger.setHost(host);
        swagger.setBasePath("/api/");
        Info info = new Info();
        info.setTitle(app.getName());
        info.setVersion("1.0.0");
        swagger.setInfo(info);
        swagger.setSchemes(Collections.singletonList(Scheme.HTTP));
        //应用鉴权
        swagger.security(new SecurityRequirement().requirement("appId"));
        swagger.security(new SecurityRequirement().requirement("appKey"));
        swagger.security(new SecurityRequirement().requirement("plat"));
        swagger.securityDefinition("appId", new ApiKeyAuthDefinition().in(In.HEADER).name("JB-AppId"));
        swagger.securityDefinition("appKey", new ApiKeyAuthDefinition().in(In.HEADER).name("JB-Key"));
        swagger.securityDefinition("plat", new ApiKeyAuthDefinition().in(In.HEADER).name("JB-Plat"));
        return swagger;
    }

    private void addClazzModels(App app, List<Clazz> clazzs, Swagger swagger) {
        //构建数据模型
        for (Clazz clazz : clazzs) {
            //创建模型
            ModelImpl model = new ModelImpl();
            //获取字段数据
            List<Field> fields = fieldService.list(app.getId(), clazz.getName());
            for (Field field : fields) {
                Property property = null;
                switch (field.getType()) {
                    case FieldType.STRING:
                        property = new StringProperty();
                        break;
                    case FieldType.NUMBER:
                        property = new DecimalProperty();
                        break;
                    case FieldType.BOOLEAN:
                        property = new BooleanProperty();
                        break;
                    case FieldType.DATE:
                        property = new DateProperty();
                        break;
                    case FieldType.OBJECT:
                        property = new ObjectProperty();
                        break;
                    case FieldType.ARRAY:
                        property = new ArrayProperty(new ObjectProperty());
                        break;
                }
                if (property != null) {
                    //设置字段名称
                    property.setName(field.getName());
                    model.addProperty(field.getName(), property);
                }
            }
            model.setType("object");
            //添加模型
            swagger.model(clazz.getName(), model);
            //添加标签
            Tag tag = new Tag();
            tag.setName(clazz.getName());
            swagger.addTag(tag);
        }
    }

    private void addClazzPath(List<Clazz> clazzs, Swagger swagger) {
        for (Clazz clazz : clazzs) {
            if (!clazz.isInternal()) {
                //根路径
                Path rootPath = new Path();
                //新增
                Operation insertOperation = new Operation();
                insertOperation.tag(clazz.getName());
                insertOperation.summary("创建对象");
                insertOperation.parameter(body(new RefModel(clazz.getName())));
                insertOperation.parameter(fetch());
                insertOperation.response(200, new Response());
                rootPath.post(insertOperation);
                //查询
                Operation findOperation = new Operation();
                findOperation.summary("查询对象");
                findOperation.tag(clazz.getName());
                findOperation.parameter(where());
                findOperation.parameter(include());
                findOperation.parameter(keys());
                findOperation.parameter(order());
                findOperation.parameter(limit());
                findOperation.parameter(skip());
                findOperation.response(200, new Response());
                rootPath.get(findOperation);
                swagger.path("/object/" + clazz.getName(), rootPath);

                //对象id路径
                Path objectPath = new Path();
                //获取对象
                Operation getOperation = new Operation();
                getOperation.summary("获取对象");
                getOperation.tag(clazz.getName());
                getOperation.parameter(id());
                getOperation.response(200, new Response());
                objectPath.get(getOperation);
                //更新对象
                Operation updateOperation = new Operation();
                updateOperation.summary("更新对象");
                updateOperation.tag(clazz.getName());
                updateOperation.parameter(id());
                updateOperation.parameter(body(new RefModel(clazz.getName())));
                updateOperation.response(200, new Response());
                objectPath.put(updateOperation);
                //删除对象
                Operation deleteOperation = new Operation();
                deleteOperation.summary("删除对象");
                deleteOperation.tag(clazz.getName());
                deleteOperation.parameter(id());
                deleteOperation.response(200, new Response());
                objectPath.delete(deleteOperation);
                swagger.path("/object/" + clazz.getName() + "/{id}/", objectPath);

                //计数路径
                Path countPath = new Path();
                Operation countOperation = new Operation();
                countOperation.summary("对象计数");
                countOperation.tag(clazz.getName());
                countOperation.parameter(where());
                countOperation.response(200, new Response());
                countPath.get(countOperation);
                swagger.path("/object/" + clazz.getName() + "/count/", countPath);
            }
        }
    }

}
