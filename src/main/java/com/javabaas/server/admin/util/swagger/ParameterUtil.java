package com.javabaas.server.admin.util.swagger;

import io.swagger.models.Model;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;

/**
 * Created by Codi on 2017/10/11.
 */
public class ParameterUtil {

    public static Parameter id() {
        return new PathParameter().name("id").type(StringProperty.TYPE).required(true);
    }

    public static Parameter where() {
        return new QueryParameter().name("where").type(StringProperty.TYPE);
    }

    public static Parameter fetch() {
        return new QueryParameter().name("fetch").type(BooleanProperty.TYPE);
    }

    public static Parameter include() {
        return new QueryParameter().name("include").type(StringProperty.TYPE);
    }

    public static Parameter keys() {
        return new QueryParameter().name("keys").type(StringProperty.TYPE);
    }

    public static Parameter order() {
        return new QueryParameter().name("order").type(StringProperty.TYPE);
    }

    public static Parameter limit() {
        return new QueryParameter().name("limit").type(IntegerProperty.TYPE);
    }

    public static Parameter skip() {
        return new QueryParameter().name("skip").type(IntegerProperty.TYPE);
    }

    public static Parameter body(Model scheme) {
        BodyParameter body = new BodyParameter();
        body.setRequired(true);
        body.setName("body");
        body.setSchema(scheme);
        return body;
    }

}
