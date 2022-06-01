package com.awyFamily.message.distribution.core.customize;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.model.sender.SenderConfig;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberConfig;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberTypeEnum;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.network.mqtt.IMqttClient;
import com.awyFamily.message.distribution.core.network.mqtt.MqttConfig;
import com.awyFamily.message.distribution.core.network.mqtt.PahoMqttClient;
import com.awyFamily.message.distribution.core.sender.ISender;
import com.awyFamily.message.distribution.core.sender.ISenderProvider;
import com.awyFamily.message.distribution.core.sender.mqtt.SimpleMqttSender;
import com.awyFamily.message.distribution.core.subscriber.ISubscriber;
import com.awyFamily.message.distribution.core.subscriber.ISubscriberProvider;
import com.awyFamily.message.distribution.core.subscriber.mqtt.SimpleMqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当实例同时存在 mqtt 的发布者和订阅者,避免连接资源的浪费
 * 其它的相关的消息中间件,实现效果和当前一致
 * @author yhw
 * @date 2022-06-01
 */
public class SimpleMqttSenderSubscriberProvider implements ISubscriberProvider<MqttMessage>, ISenderProvider<SimpleMqttSender> {


    private final Map<String, ISender> senderRepository = new ConcurrentHashMap<>();
    private final Map<String, ISubscriber> subscriberRepository = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientIdBindTopicsRepository = new ConcurrentHashMap<>();
    private final Map<String, IMqttClient> clientRepository = new ConcurrentHashMap<>();

    @Override
    public SenderTypeEnum getSenderType() {
        return SenderTypeEnum.mqtt;
    }

    @Override
    public ISender createSender(SenderConfig config) {
        ISender sender = senderRepository.get(config.getTopic());
        if (sender == null) {
            MqttConfig mqttConfig = JSONUtil.toBean(config.getContent(),MqttConfig.class);
            IMqttClient client = clientRepository.get(mqttConfig.getClientId());
            if (client == null) {
                client = new PahoMqttClient(mqttConfig);
                clientRepository.put(mqttConfig.getClientId(),client);
            }
            sender = new SimpleMqttSender(config.getTopic(),client);
            senderRepository.put(config.getTopic(),sender);

            Set<String> topics = clientIdBindTopicsRepository.getOrDefault(mqttConfig.getClientId(), new HashSet<>());
            topics.add(config.getTopic());
            clientIdBindTopicsRepository.put(mqttConfig.getClientId(),topics);
        }
        return sender;
    }

    @Override
    public Mono<Void> removeSender(String topic) {
        ISender sender = senderRepository.remove(topic);
        if (sender == null) {
            return Mono.error(() -> {
                return new RuntimeException("sender not exists");
            });
        }
        //get clientId
        String clientId = "";
        for (Map.Entry<String, Set<String>> entry : clientIdBindTopicsRepository.entrySet()) {
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

        Set<String> topicSet = clientIdBindTopicsRepository.get(clientId);
        topicSet.remove(topic);

        if (CollUtil.isEmpty(topicSet)) {
            clientIdBindTopicsRepository.remove(clientId);
            IMqttClient client = clientRepository.remove(topic);
            client.close();
        }
        return Mono.empty();
    }

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
            Set<String> set = clientIdBindTopicsRepository.getOrDefault(mqttConfig.getClientId(), new HashSet<>());
            set.add(config.getTopic());
            clientIdBindTopicsRepository.put(mqttConfig.getClientId(),set);
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
        for (Map.Entry<String, Set<String>> entry : clientIdBindTopicsRepository.entrySet()) {
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


        Set<String> topicSet = clientIdBindTopicsRepository.get(clientId);
        topicSet.remove(topic);
        subscriber.unsubscribe().subscribe();

        //当 client 没绑定 topic , 则释放资源
        if (CollUtil.isEmpty(topicSet)) {
            clientIdBindTopicsRepository.remove(clientId);
            IMqttClient client = clientRepository.remove(clientId);
            client.close();
        }
        return Mono.empty();
    }
}
