package com.awyFamily.message.distribution.core.subscriber.rabbit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberConfig;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberTypeEnum;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.network.message.VertxRabbitMessage;
import com.awyFamily.message.distribution.core.network.rabbit.IRabbitClient;
import com.awyFamily.message.distribution.core.network.rabbit.RabbitConfig;
import com.awyFamily.message.distribution.core.network.rabbit.VertxRabbitClient;
import com.awyFamily.message.distribution.core.subscriber.ISubscriber;
import com.awyFamily.message.distribution.core.subscriber.ISubscriberProvider;
import io.vertx.core.Vertx;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yhw
 * @date 2022-05-31
 */
public class SimpleRabbitSubscriberProvider implements ISubscriberProvider<VertxRabbitMessage> {


    private final Map<String, ISubscriber> subscriberRepository = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientIdBindTopicsMap = new ConcurrentHashMap<>();
    private final Map<String, IRabbitClient> clientRepository = new ConcurrentHashMap<>();

    @Override
    public SubscriberTypeEnum getSubscriberType() {
        return SubscriberTypeEnum.rabbit_mq;
    }

    @Override
    public ISubscriber createSubscriber(SubscriberConfig config, Collection<ISubscribeHandler<VertxRabbitMessage>> handlers) {
        ISubscriber subscriber = subscriberRepository.get(config.getTopic());
        if (subscriber == null) {
            RabbitConfig rabbitConfig = JSONUtil.toBean(config.getContent(), RabbitConfig.class);
            IRabbitClient client = clientRepository.get(rabbitConfig.getClientId());
            if (client == null) {
                client = new VertxRabbitClient(rabbitConfig, Vertx.vertx());
                clientRepository.put(rabbitConfig.getClientId(),client);
            }
            subscriber = new SimpleRabbitSubscriber(config.getTopic(),client,handlers);
            subscriberRepository.put(config.getTopic(),subscriber);

            //clientIdBindTopicsMap bind
            Set<String> set = clientIdBindTopicsMap.getOrDefault(rabbitConfig.getClientId(), new HashSet<>());
            set.add(config.getTopic());
            clientIdBindTopicsMap.put(rabbitConfig.getClientId(),set);
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
                return new RuntimeException("rabbit client not exists");
            });
        }


        Set<String> topicSet = clientIdBindTopicsMap.get(clientId);
        topicSet.remove(topic);
        subscriber.unsubscribe().subscribe();

        //当 client 没绑定 topic , 则释放资源
        if (CollUtil.isEmpty(topicSet)) {
            clientIdBindTopicsMap.remove(clientId);
            IRabbitClient client = clientRepository.remove(clientId);
            client.close();
        }
        return Mono.empty();
    }
}
