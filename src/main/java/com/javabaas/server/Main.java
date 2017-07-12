package com.javabaas.server;

import com.javabaas.server.common.interceptor.AdminInterceptor;
import com.javabaas.server.common.interceptor.AuthInterceptor;
import com.javabaas.server.common.interceptor.HeaderInterceptor;
import com.javabaas.server.common.interceptor.MasterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class Main extends WebMvcConfigurerAdapter {

    @Autowired
    private HeaderInterceptor contentTypeInterceptor;
    @Autowired
    private AuthInterceptor authInterceptor;
    @Autowired
    private AdminInterceptor adminInterceptor;
    @Autowired
    private MasterInterceptor masterInterceptor;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //请求头拦截器，检查Content-Type、platform等
        registry.addInterceptor(contentTypeInterceptor).addPathPatterns("/api/**").excludePathPatterns("/api/file/callback",
                "/api/file/notify/**", "/customer/**");
        //授权拦截器
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**").excludePathPatterns("/api/file/callback",
                "/api/file/notify/**", "/api/admin/**", "/customer/**");
        //超级权限
        registry.addInterceptor(adminInterceptor).addPathPatterns("/api/admin/**");
        //管理权限
        registry.addInterceptor(masterInterceptor).addPathPatterns("/api/master/**");
    }

    @Bean
    public RestTemplate getRest() {
        return new RestTemplate();
    }

    @Bean
    public MappingMongoConverter mongoConverter(MongoDbFactory mongoFactory, MappingContext<? extends MongoPersistentEntity<?>,
            MongoPersistentProperty> mongoMappingContext) throws Exception {
        //自动将mongoDB中存储的key中的.替换为_
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        mongoConverter.setMapKeyDotReplacement("_");
        return mongoConverter;
    }

}
