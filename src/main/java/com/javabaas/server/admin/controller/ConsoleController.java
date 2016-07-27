package com.javabaas.server.admin.controller;

import com.javabaas.server.admin.service.ConsoleService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.entity.SimpleResult;
import com.javabaas.server.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理端鉴权
 * Created by Codi on 15/10/20.
 */
@RestController
@RequestMapping(value = "/console")
public class ConsoleController {

    @Autowired
    private ConsoleService consoleService;
    @Autowired
    private AuthConfig authConfig;

    @RequestMapping(value = "/adminKey", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult adminKey(HttpServletRequest request) {
        SimpleResult result = SimpleResult.success();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new SimpleError(SimpleCode.CONSOLE_NOT_LOGIN);
        }
        String sessionToken = getCookieValue(cookies, "JB-sessionToken");
        String username = getCookieValue(cookies, "JB-username");
        if (StringUtils.isEmpty(sessionToken) || StringUtils.isEmpty(username)) {
            throw new SimpleError(SimpleCode.CONSOLE_NOT_LOGIN);
        } else {
            String sessionTokenExist = consoleService.getSessionToken(username);
            if (!sessionToken.equals(sessionTokenExist)) {
                throw new SimpleError(SimpleCode.CONSOLE_SESSION_TOKEN_ERROR);
            } else {
                result.put("key", authConfig.getKey());
            }
        }
        return result;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult login(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        String sessionToken = consoleService.getSessionToken(username, password);
        response.addCookie(new Cookie("JB-username", username));
        response.addCookie(new Cookie("JB-sessionToken", sessionToken));
        return SimpleResult.success();
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public SimpleResult logout(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new SimpleError(SimpleCode.CONSOLE_NOT_LOGIN);
        }
        String username = getCookieValue(cookies, "JB-username");
        if (username != null) {
            consoleService.removeSessionToken(username);
        }
        return SimpleResult.success();
    }

    private String getCookieValue(Cookie[] cookies, String key) {
        for (Cookie cookie : cookies) {
            if (key.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
