package com.awyFamily.message.distribution.core.subscriber.rabbit;

import com.awyFamily.message.distribution.core.model.subscriber.SubscriberTypeEnum;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.network.message.VertxRabbitMessage;
import com.awyFamily.message.distribution.core.network.rabbit.IRabbitClient;
import com.awyFamily.message.distribution.core.subscriber.ISubscriber;
import lombok.Getter;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * @author yhw
 * @date 2022-05-31
 */
public class SimpleRabbitSubscriber implements ISubscriber {

    @Getter
    private String topic;
    private IRabbitClient client;

    public SimpleRabbitSubscriber(String queue, IRabbitClient client, Collection<ISubscribeHandler<VertxRabbitMessage>> handlers) {
        this.topic = queue;
        this.client = client;
        this.client.addSubscribeHandler(queue,handlers);
    }


    @Override
    public Mono<Void> subscribe() {
        return Mono.just(client.subscribe(topic)).map(b -> {
            if (!b) {
                throw new RuntimeException("subscribe top : "+ topic +" error");
            }
            return b;
        }).then();
    }

    @Override
    public Mono<Void> unsubscribe() {
        return Mono.just(client.unsubscribe(getTopic())).then();
    }

    @Override
    public SubscriberTypeEnum getType() {
        return SubscriberTypeEnum.mqtt;
    }
}
