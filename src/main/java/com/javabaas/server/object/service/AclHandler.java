package com.javabaas.server.object.service;

import com.javabaas.server.object.entity.BaasAcl;
import com.javabaas.server.object.entity.BaasList;
import com.javabaas.server.object.entity.BaasObject;
import com.javabaas.server.object.entity.BaasQuery;
import com.javabaas.server.user.entity.BaasUser;
import org.springframework.stereotype.Service;

/**
 * Created by Codi on 2017/9/12.
 */
@Service
public class AclHandler {

    /**
     * 添加ACL权限检查
     *
     * @param query    原查询
     * @param user     用户
     * @param isMaster 是否为管理权限
     * @return 合成ACL权限检查以后的查询
     */
    public BaasQuery handleAcl(BaasQuery query, BaasUser user, boolean isMaster) {
        //处理ACL权限
        if (!isMaster) {
            //非master权限则检查ACL
            BaasAcl aclQuery;
            //全局读权限
            BaasAcl aclAll = new BaasAcl("acl.*.read", true);
            if (user == null) {
                aclQuery = aclAll;
            } else {
                //添加登录用户所拥有的读取权限
                String id = user.getId();
                //用户读权限
                BaasList acls = new BaasList();
                acls.add(aclAll);
                acls.add(new BaasObject("acl." + id + ".read", true));
                aclQuery = new BaasAcl("$or", acls);
            }
            //将ACL权限查询合并到主查询
            BaasQuery newQuery = new BaasQuery();
            BaasList list = new BaasList();
            list.add(aclQuery);
            if (query != null) {
                list.add(query);
            }
            newQuery.put("$and", list);
            return newQuery;
        }
        return query;
    }

}
