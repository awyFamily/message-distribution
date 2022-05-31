package com.awyFamily.message.distribution.core.subscriber;

import com.awyFamily.message.distribution.core.model.subscriber.SubscriberConfig;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberTypeEnum;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * @author yhw
 * @date 2022-05-26
 */
public interface ISubscriberProvider<T> {

    /**
     * @return Subscriber 类型
     */
    SubscriberTypeEnum getSubscriberType();

    default String getSubscriberTypeName() {
        return this.getSubscriberType().getName();
    }

    /**
     * 创建并获取 Subscriber
     * @param config 创建所需的配置信息
     * @return
     */
    ISubscriber createSubscriber(SubscriberConfig config, Collection<ISubscribeHandler<T>> handlers);


    Mono<Void> removeSubscriber(String topic);

}
