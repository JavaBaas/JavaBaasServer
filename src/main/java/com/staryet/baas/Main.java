package com.staryet.baas;

import com.staryet.baas.common.interceptor.AdminInterceptor;
import com.staryet.baas.common.interceptor.AuthInterceptor;
import com.staryet.baas.common.interceptor.HeaderInterceptor;
import com.staryet.baas.common.interceptor.MasterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
        registry.addInterceptor(contentTypeInterceptor).addPathPatterns("/api/**").excludePathPatterns("/api/file/callback", "/api/file/notify/**", "/customer/**");
        //授权拦截器
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**").excludePathPatterns("/api/file/callback", "/api/file/notify/**", "/api/admin/**", "/customer/**");
        //超级权限
        registry.addInterceptor(adminInterceptor).addPathPatterns("/api/admin/**");
        //管理权限
        registry.addInterceptor(masterInterceptor).addPathPatterns("/api/master/**");
    }

    @Bean
    public RestTemplate getRest() {
        return new RestTemplate();
    }

}
