package com.awyFamily.message.distribution.core.network.mqtt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.awyFamily.message.distribution.core.network.BaseClient;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.network.message.VertxMqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author yhw
 * @date 2022-05-27
 */
@Slf4j
public class VertxMqttClient extends BaseClient<VertxMqttMessage> implements IMqttClient<VertxMqttMessage> {

    private MqttClient client;

    private MqttConfig config;

    public VertxMqttClient(MqttConfig config,Vertx vertx) {
        this.config = config;
        init(vertx);
    }

    private void init(Vertx vertx) {
        MqttClientOptions options = new MqttClientOptions();
        options.setUsername(config.getUsername());
        options.setPassword(config.getPassword());
        options.setReconnectInterval(3);
        options.setConnectTimeout(config.getConnectionTimeout());
        options.setKeepAliveInterval(config.getKeepAliveInterval());
        options.setAutoKeepAlive(true);
        options.setClientId(StrUtil.isBlank(config.getClientId()) ? UUID.randomUUID().toString() : config.getClientId());

        this.client = MqttClient.create(vertx,options);
        client.closeHandler(ch -> {
            log.info("connect mqtt [{}] close", config.getHost(), config.getClientId());
        }).publishHandler(msg -> {
            Set<ISubscribeHandler<VertxMqttMessage>> handlers = getHandlerMap().getOrDefault(msg.topicName(),new HashSet<>())
                    .stream().sorted(Comparator.comparing(ISubscribeHandler::getSort)).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(handlers)) {
                VertxMqttMessage mqttMessage = new VertxMqttMessage(msg.messageId(), msg.qosLevel().value(), msg.topicName(), msg.payload());
                for (ISubscribeHandler<VertxMqttMessage> handler : handlers) {
                    if (handler.handler(mqttMessage)) {
                        break;
                    }
                }
            }
        });
        client.connect(config.getPort(),config.getHost(),result -> {
            if (!result.succeeded()) {
                log.warn("connect mqtt [{}] error", config.getHost(), config.getClientId(), result.cause());
            } else {
                log.info("connect mqtt [{}] success", config.getHost(), config.getClientId());
            }
        });
    }

    @Override
    public boolean publish(String topic, int qos, VertxMqttMessage message) {
        client.publish(topic,message.getPayload(), MqttQoS.valueOf(qos),false,false,h -> {
            if (!h.succeeded()) {
                log.warn("publish topic [{}] error", topic, h.cause());
            } else {
                log.info("publish topic [{}] success", topic);
            }
        });
        return true;
    }

    @Override
    public boolean subscribe(String topic, int qos) {
        if (!isAvailable()) {
            return false;
        }
        client.subscribe(topic, qos, h -> {
            if (!h.succeeded()) {
                log.warn("subscribe topic [{}] error", topic, h.cause());
            } else {
                log.info("subscribe topic [{}] success", topic);
            }
        });
        return true;
    }

    @Override
    public boolean unsubscribe(String topic) {
        if (!isAvailable()) {
            return false;
        }
        client.unsubscribe(topic,h -> {
            if (!h.succeeded()) {
                log.warn("unsubscribe topic [{}] error", topic, h.cause());
            } else {
                log.info("unsubscribe topic [{}] success", topic);
            }
        });
        return true;
    }

    @Override
    public boolean isAvailable() {
        return client != null && client.isConnected();
    }

    @Override
    public boolean close() {
        if (isAvailable()) {
            client.disconnect(h -> {
                if (!h.succeeded()) {
                    log.warn("disconnect mqtt [{}] error", config.getHost(), config.getClientId(), h.cause());
                } else {
                    log.info("disconnect mqtt [{}] success", config.getHost(), config.getClientId());
                }
            });
            return true;
        }
        return false;
    }
}
