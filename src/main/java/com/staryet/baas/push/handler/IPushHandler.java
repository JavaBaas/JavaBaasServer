package com.staryet.baas.push.handler;

import com.staryet.baas.push.entity.Push;

import java.util.Collection;

/**
 * 推送接口
 * Created by Codi on 15/11/2.
 */
public interface IPushHandler {

    /**
     * 推送给特定设备
     *
     * @param id   installationId
     * @param push 推送消息
     */
    void pushSingle(String appId, String id, Push push);

    /**
     * 推送给多个设备
     *
     * @param ids  id列表
     * @param push 推送消息
     */
    void pushMulti(String appId, Collection<String> ids, Push push);

    /**
     * 推送到所有设备
     *
     * @param push 推送消息
     */
    void pushAll(String appId, Push push);

}
