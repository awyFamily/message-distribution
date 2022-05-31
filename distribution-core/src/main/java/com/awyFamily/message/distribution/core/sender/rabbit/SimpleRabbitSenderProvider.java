package com.awyFamily.message.distribution.core.sender.rabbit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.model.sender.SenderConfig;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.network.mqtt.IMqttClient;
import com.awyFamily.message.distribution.core.network.rabbit.IRabbitClient;
import com.awyFamily.message.distribution.core.network.rabbit.RabbitConfig;
import com.awyFamily.message.distribution.core.network.rabbit.VertxRabbitClient;
import com.awyFamily.message.distribution.core.sender.ISender;
import com.awyFamily.message.distribution.core.sender.ISenderProvider;
import io.vertx.core.Vertx;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yhw
 * @date 2022-05-31
 */
public class SimpleRabbitSenderProvider implements ISenderProvider<SimpleRabbitSender> {

    private final Map<String, ISender> senderRepository = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientIdBindTopicsRepository = new ConcurrentHashMap<>();
    private final Map<String, IRabbitClient> clientRepository = new ConcurrentHashMap<>();

    @Override
    public SenderTypeEnum getSenderType() {
        return SenderTypeEnum.rabbit_mq;
    }

    @Override
    public ISender createSender(SenderConfig config) {
        ISender sender = senderRepository.get(config.getTopic());
        if (sender == null) {
            RabbitConfig rabbitConfig = JSONUtil.toBean(config.getContent(), RabbitConfig.class);
            IRabbitClient client = clientRepository.get(rabbitConfig.getClientId());
            if (client == null) {
                client = new VertxRabbitClient(rabbitConfig, Vertx.vertx());
                clientRepository.put(rabbitConfig.getClientId(),client);
            }
            sender = new SimpleRabbitSender(client,config.getTopic(),rabbitConfig.getRoutingKey());
            senderRepository.put(config.getTopic(),sender);

            Set<String> topics = clientIdBindTopicsRepository.getOrDefault(rabbitConfig.getClientId(), new HashSet<>());
            topics.add(config.getTopic());
            clientIdBindTopicsRepository.put(rabbitConfig.getClientId(),topics);
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
                return new RuntimeException("rabbit client not exists");
            });
        }

        Set<String> topicSet = clientIdBindTopicsRepository.get(clientId);
        topicSet.remove(topic);

        if (CollUtil.isEmpty(topicSet)) {
            clientIdBindTopicsRepository.remove(clientId);
            IRabbitClient client = clientRepository.remove(topic);
            client.close();
        }
        return Mono.empty();
    }
}
