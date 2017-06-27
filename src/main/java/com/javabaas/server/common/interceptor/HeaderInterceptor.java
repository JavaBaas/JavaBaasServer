package com.javabaas.server.common.interceptor;

import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 检查请求头是否为json格式
 * Created by Codi on 15/10/7.
 */
@Component
public class HeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String contentType = request.getHeader("Content-Type");
        if (request.getMethod().equals("OPTIONS")) {
            //遇到OPTIONS请求时直接返回成功
            response.setStatus(200);
            return false;
        }
        if (StringUtils.isEmpty(contentType) || !contentType.contains("application/json")) {
            //检查contentType
            response.setStatus(400);
            response.getOutputStream().write("ContentType must be application/json".getBytes());
            return false;
        }
        if (!checkPlatform(request.getHeader("JB-Plat"))) {
            //检查platform
            throw new SimpleError(SimpleCode.REQUEST_PLATFORM_ERROR);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    private boolean checkPlatform(String platform) {
        return !(StringUtils.isEmpty(platform) ||
                !platform.equals("android") &&
                        !platform.equals("ios") &&
                        !platform.equals("js") &&
                        !platform.equals("cloud") &&
                        !platform.equals("shell") &&
                        !platform.equals("admin"));
    }
}
