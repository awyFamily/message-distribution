package com.awyFamily.distribution.admin.controller;

import com.awyFamily.message.distribution.core.manager.ISubscriberManager;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.network.message.VertxRabbitMessage;
import com.awyFamily.message.distribution.core.subscriber.ISubscriber;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yhw
 * @date 2021-11-02
 */
@RestController
public class SimpleSubscriberController {

    @Autowired
    private ISubscriberManager subscriberManager;


    @GetMapping("/sub/{id}")
    public Mono subscribe(@PathVariable("id")String id) {
        ISubscriber subscriber = null;
        List<ISubscribeHandler<MqttMessage>> handlers = new ArrayList<>();
        if ("1".equals(id)) {
            handlers.add(new ISubscribeHandler<MqttMessage>() {

                private String message;
                @Override
                public boolean apply(MqttMessage mqttMessage) {
                    System.out.println(message);
                    return true;
                }

                @Override
                public boolean match(MqttMessage mqttMessage) {
                    message = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
                    if (message.contains("off")) {
                        return true;
                    }
                    return false;
                }

                @Override
                public int getSort() {
                    return 1;
                }
            });
            subscriber = subscriberManager.getSubscriber(id,handlers);
        } else if ("2".equals(id)) {
            handlers.add(new ISubscribeHandler<MqttMessage>() {

                //                private String message;
                @Override
                public boolean apply(MqttMessage mqttMessage) {
                    System.err.print(1);
                    return true;
                }

                @Override
                public boolean match(MqttMessage mqttMessage) {
//                    message = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
//                    if (message.contains("off")) {
//                        return true;
//                    }
                    return true;
                }

                @Override
                public int getSort() {
                    return 1;
                }
            });
            subscriber = subscriberManager.getSubscriber(id,handlers);
        } else if ("3".equals(id)) {
            List<ISubscribeHandler<VertxRabbitMessage>> rabbitHandlers = new ArrayList<>();

            rabbitHandlers.add(new ISubscribeHandler<VertxRabbitMessage>() {

                //                private String message;
                @Override
                public boolean apply(VertxRabbitMessage mqttMessage) {
                    System.err.print(1);
                    return true;
                }

                @Override
                public boolean match(VertxRabbitMessage mqttMessage) {
                    System.out.println(mqttMessage.getPayload());
//                    message = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
//                    if (message.contains("off")) {
//                        return true;
//                    }
                    return true;
                }

                @Override
                public int getSort() {
                    return 1;
                }
            });
            subscriber = subscriberManager.getSubscriber(id, rabbitHandlers);
        }
        if (subscriber == null) {
            return Mono.error(() -> {
                return new RuntimeException("订阅者为空");
            });
        }
//        subscriber.subscribe().subscribe();
        return subscriber.subscribe();
    }

    @GetMapping("/unsub/{id}")
    public String unsubscribe(@PathVariable("id")String id) {
        subscriberManager.remove(id);
        return "unsub";
    }
}
