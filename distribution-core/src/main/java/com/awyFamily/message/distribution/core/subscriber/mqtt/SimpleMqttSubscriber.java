package com.awyFamily.message.distribution.core.subscriber.mqtt;

import com.awyFamily.message.distribution.core.model.subscriber.SubscriberTypeEnum;
import com.awyFamily.message.distribution.core.network.mqtt.IMqttClient;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.subscriber.ISubscriber;
import lombok.Getter;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * @author yhw
 * @date 2022-05-26
 */
public class SimpleMqttSubscriber implements ISubscriber {

    @Getter
    private String topic;
    private IMqttClient client;

    public SimpleMqttSubscriber(String topic, IMqttClient client, Collection<ISubscribeHandler<MqttMessage>> handlers) {
        this.topic = topic;
        this.client = client;
        this.client.addSubscribeHandler(topic,handlers);
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
