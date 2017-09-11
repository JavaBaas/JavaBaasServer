package com.javabaas.server.object.service;

import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.entity.ClazzAclMethod;
import com.javabaas.server.admin.service.ClazzService;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.user.entity.BaasUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Codi on 2017/8/3.
 */
@Component
public class ClazzAclChecker {

    @Autowired
    private ClazzService clazzService;

    public void verifyClazzAccess(String appId, ClazzAclMethod method, String className, BaasUser user, boolean isMaster) {
        Clazz clazz = clazzService.get(appId, className);
        if (clazz.getAcl() != null) {
            //验证表级ACL权限
            if (!isMaster) {
                //非master权限验证acl
                if (!clazz.getAcl().hasAccess(method, user)) {
                    //无操作权限
                    throw new SimpleError(SimpleCode.OBJECT_CLAZZ_NO_ACCESS);
                }
            }
        }
    }

}
