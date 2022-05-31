package com.awyFamily.message.distribution.core.sender.mqtt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.model.sender.SenderConfig;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.network.mqtt.IMqttClient;
import com.awyFamily.message.distribution.core.network.mqtt.MqttConfig;
import com.awyFamily.message.distribution.core.network.mqtt.PahoMqttClient;
import com.awyFamily.message.distribution.core.sender.ISender;
import com.awyFamily.message.distribution.core.sender.ISenderProvider;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yhw
 * @date 2021-11-24
 */
public class SimpleMqttSenderProvider implements ISenderProvider<SimpleMqttSender> {

    private final Map<String, ISender> senderRepository = new ConcurrentHashMap<>();
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
}
