package com.awyFamily.message.distribution.core.subscriber;

import com.awyFamily.message.distribution.core.model.subscriber.SubscriberTypeEnum;
import reactor.core.publisher.Mono;

/**
 * @author yhw
 * @date 2022-05-26
 */
public interface ISubscriber {

    /**
     * 订阅主题标识(唯一标识)
     * @return 唯一标识
     */
    String getTopic();

    Mono<Void> subscribe();

    Mono<Void> unsubscribe();


    /**
     * 提供者类型
     * @return 提供者类型
     */
    SubscriberTypeEnum getType();

    default String getSubscriberTypeName() {
        return this.getType().getName();
    }

}
