package com.awyFamily.message.distribution.core.subscriber.mqtt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberConfig;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberTypeEnum;
import com.awyFamily.message.distribution.core.network.mqtt.IMqttClient;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.network.mqtt.PahoMqttClient;
import com.awyFamily.message.distribution.core.network.mqtt.MqttConfig;
import com.awyFamily.message.distribution.core.subscriber.ISubscriber;
import com.awyFamily.message.distribution.core.subscriber.ISubscriberProvider;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yhw
 * @date 2022-05-26
 */
public class SimpleMqttSubscriberProvider implements ISubscriberProvider<MqttMessage> {

    private final Map<String, ISubscriber> subscriberRepository = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientIdBindTopicsMap = new ConcurrentHashMap<>();
    private final Map<String, IMqttClient> clientRepository = new ConcurrentHashMap<>();


    @Override
    public SubscriberTypeEnum getSubscriberType() {
        return SubscriberTypeEnum.mqtt;
    }

    @Override
    public ISubscriber createSubscriber(SubscriberConfig config, Collection<ISubscribeHandler<MqttMessage>> handlers) {
        ISubscriber subscriber = subscriberRepository.get(config.getTopic());
        if (subscriber == null) {
            MqttConfig mqttConfig = JSONUtil.toBean(config.getContent(),MqttConfig.class);
            IMqttClient client = clientRepository.get(mqttConfig.getClientId());
            if (client == null) {
                client = new PahoMqttClient(mqttConfig);
                clientRepository.put(mqttConfig.getClientId(),client);
            }
            subscriber = new SimpleMqttSubscriber(config.getTopic(),client,handlers);
            subscriberRepository.put(config.getTopic(),subscriber);

            //clientIdBindTopicsMap bind
            Set<String> set = clientIdBindTopicsMap.getOrDefault(mqttConfig.getClientId(), new HashSet<>());
            set.add(config.getTopic());
            clientIdBindTopicsMap.put(mqttConfig.getClientId(),set);
        }
        return subscriber;
    }

    @Override
    public Mono<Void> removeSubscriber(String topic) {
        ISubscriber subscriber = subscriberRepository.remove(topic);
        if (subscriber == null) {
            return Mono.error(() -> {
                return new RuntimeException("subscriber not exists");
            });
        }
        //get clientId
        String clientId = "";
        for (Map.Entry<String, Set<String>> entry : clientIdBindTopicsMap.entrySet()) {
            if (entry.getValue().contains(topic)) {
                clientId = entry.getKey();
                break;
            }
        }
        if (StrUtil.isBlank(clientId)) {
            return Mono.error(() -> {
                return new RuntimeException("mqtt client not exists");
            });
        }


        Set<String> topicSet = clientIdBindTopicsMap.get(clientId);
        topicSet.remove(topic);
        subscriber.unsubscribe().subscribe();

        //当 client 没绑定 topic , 则释放资源
        if (CollUtil.isEmpty(topicSet)) {
            clientIdBindTopicsMap.remove(clientId);
            IMqttClient client = clientRepository.remove(clientId);
            client.close();
        }
        return Mono.empty();
    }
}
