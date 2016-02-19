package com.staryet.baas.common.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Staryet on 15/8/14.
 */
@Service
public class MasterService {

    public boolean isMaster(HttpServletRequest request) {
        String masterSign = request.getHeader("JB-MasterSign");
        if (!StringUtils.isEmpty(masterSign)) {
            return true;
        } else {
            return false;
        }
    }

}
