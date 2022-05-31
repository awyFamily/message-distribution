package com.awyFamily.message.distribution.core.manager;

import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.subscriber.ISubscriber;

import java.util.Collection;

/**
 * @author yhw
 * @date 2022-05-26
 */
public interface ISubscriberManager<T> {


    /**
     * 通过资源ID和发送者类型获取消息订阅者
     * @param id 资源ID(获取配置)
     *           可以是资源的唯一标识,例如设备的唯一编号
     * @return 消息发送者列表
     */
    ISubscriber getSubscriber(String id, Collection<ISubscribeHandler<T>> handlers);

    /**
     * 移除
     * @param id 资源ID
     */
    void remove(String id);
}
