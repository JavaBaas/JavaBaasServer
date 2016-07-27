package com.javabaas.server.admin.service;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.admin.entity.Clazz;
import com.javabaas.server.admin.entity.ClazzAcl;
import com.javabaas.server.admin.entity.ClazzAclMethod;
import com.javabaas.server.admin.entity.dto.ClazzExport;
import com.javabaas.server.admin.entity.dto.FieldExport;
import com.javabaas.server.admin.repository.ClazzRepository;
import com.javabaas.server.common.entity.SimpleCode;
import com.javabaas.server.common.entity.SimpleError;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.object.service.ObjectService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 类处理
 * Created by Staryet on 15/6/19.
 */
@Service
public class ClazzService {

    private Log log = LogFactory.getLog(getClass());

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ClazzRepository clazzRepository;
    @Autowired
    private AppService appService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private JSONUtil jsonUtil;

    public Clazz get(String appId, String name) {
        Clazz clazz = getClazzCache(appId, name);
        if (clazz == null) {
            //未找到缓存
            clazz = clazzRepository.findByAppIdAndName(appId, name);
            if (clazz == null) {
                throw new SimpleError(SimpleCode.CLAZZ_NOT_FOUND);
            }
            setClazzCache(appId, name, clazz);
        }
        return clazz;
    }

    public boolean exist(String appId, String name) {
        Clazz clazz = clazzRepository.findByAppIdAndName(appId, name);
        return clazz != null;
    }

    /**
     * 插入类
     *
     * @param appId 应用id
     * @param clazz 类
     * @param valid 是否检查名称
     * @throws SimpleError
     */
    public void insert(String appId, Clazz clazz, boolean valid) {
        String name = clazz.getName();
        if (StringUtils.isEmpty(name)) {
            throw new SimpleError(SimpleCode.CLAZZ_NAME_ERROR);
        }
        if (valid && !isNameValid(name)) {
            throw new SimpleError(SimpleCode.CLAZZ_NAME_ERROR);
        }
        Clazz exist = clazzRepository.findByAppIdAndName(appId, name);
        if (exist == null) {
            clazz.setId(null);
            App app = appService.get(appId);
            clazz.setApp(app);
            if (clazz.getAcl() == null) {
                //创建默认acl
                ClazzAcl acl = new ClazzAcl();
                acl.setPublicAccess(ClazzAclMethod.INSERT, true);
                acl.setPublicAccess(ClazzAclMethod.DELETE, true);
                acl.setPublicAccess(ClazzAclMethod.UPDATE, true);
                acl.setPublicAccess(ClazzAclMethod.FIND, true);
                clazz.setAcl(acl);
            }
            clazzRepository.insert(clazz);
            //删除缓存
            deleteClazzCache(appId, name);
            log.info("App:" + appId + " Class:" + name + " 创建成功");
        } else {
            throw new SimpleError(SimpleCode.CLAZZ_ALREADY_EXIST);
        }
    }

    public void insert(String appId, Clazz clazz) {
        insert(appId, clazz, true);
    }

    public void delete(String appId, String name, boolean force) {
        Clazz clazz = get(appId, name);
        if (clazz == null) {
            throw new SimpleError(SimpleCode.CLAZZ_NOT_FOUND);
        }
        if (!force && clazz.isInternal()) {
            throw new SimpleError(SimpleCode.CLAZZ_INTERNAL);
        }
        //同时删除字段
        fieldService.deleteAll(appId, name);
        //删除缓存
        deleteClazzCache(appId, name);
        //删除数据
        objectService.deleteAll(appId, name);
        log.debug("App:" + appId + " Class:" + name + " 数据删除成功");
        //删除类
        clazzRepository.delete(clazz);
        log.info("App:" + appId + " Class:" + name + " 已删除");
    }

    public void delete(String appId, String name) {
        delete(appId, name, false);
    }

    public void deleteAll(String appId) {
        List<Clazz> clazzes = list(appId);
        for (Clazz clazz : clazzes) {
            delete(appId, clazz.getName(), true);
        }
    }

    public List<Clazz> list(String appId) {
        //类名排序
        Sort.Order o1 = new Sort.Order(Sort.Direction.DESC, "internal");
        Sort.Order o2 = new Sort.Order(Sort.Direction.ASC, "name");
        Sort sort = new Sort(o1, o2);
        PageRequest pager = new PageRequest(0, 1000, sort);
        List<Clazz> clazzs = clazzRepository.findByAppId(appId, pager);
        //统计对象个数
        clazzs.forEach(clazz -> clazz.setCount(objectService.count(appId, clazz.getName(), null, null, true)));
        return clazzs;
    }

    public List<ClazzExport> export(String appId) {
        List<Clazz> clazzs = list(appId);
        List<ClazzExport> clazzExports = new LinkedList<>();
        for (Clazz clazz : clazzs) {
            ClazzExport clazzExport = new ClazzExport();
            BeanUtils.copyProperties(clazz, clazzExport);
            clazzExports.add(clazzExport);
            //获取字段信息
            clazzExport.setFields(fieldService.export(appId, clazz.getName()));
        }
        return clazzExports;
    }

    public ClazzExport export(String appId, String clazzName) {
        Clazz clazz = get(appId, clazzName);
        ClazzExport clazzExport = new ClazzExport();
        BeanUtils.copyProperties(clazz, clazzExport);
        //获取字段信息
        clazzExport.setFields(fieldService.export(appId, clazz.getName()));
        return clazzExport;
    }

    public void importData(String appId, ClazzExport clazzExport) {
        //导入类信息
        Clazz clazz = new Clazz();
        BeanUtils.copyProperties(clazzExport, clazz);
        insert(appId, clazz, false);
        //导入字段信息
        List<FieldExport> fields = clazzExport.getFields();
        for (FieldExport fieldExport : fields) {
            fieldService.importData(appId, clazz.getName(), fieldExport);
        }
    }

    /**
     * 检查类名是否合法 (字母开头，只包含字母数字下划线)
     *
     * @param name
     * @return
     */
    public boolean isNameValid(String name) {
        String regex = "^[a-zA-Z][a-zA-Z0-9_]*$";
        return Pattern.matches(regex, name);
    }

    public void setAcl(String appId, String name, ClazzAcl acl) {
        Clazz clazz = get(appId, name);
        clazz.setAcl(acl);
        clazzRepository.save(clazz);
        log.info("App:" + appId + " Class:" + name + " ACL更新 " + jsonUtil.writeValueAsString(acl));
        deleteClazzCache(appId, name);
    }

    private Clazz getClazzCache(String appId, String name) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String string = ops.get("App_" + appId + "_Clazz_" + name);
        return jsonUtil.readValue(string, Clazz.class);
    }

    private void setClazzCache(String appId, String name, Clazz clazz) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("App_" + appId + "_Clazz_" + name, jsonUtil.writeValueAsString(clazz));
    }

    private void deleteClazzCache(String appId, String name) {
        redisTemplate.delete("App_" + appId + "_Clazz_" + name);
        log.debug("App:" + appId + " Class:" + name + " 缓存已清除");
    }

}
