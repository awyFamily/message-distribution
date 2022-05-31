package com.awyFamily.message.distribution.core.network.rabbit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.awyFamily.message.distribution.core.network.BaseClient;
import com.awyFamily.message.distribution.core.network.message.VertxRabbitMessage;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.rabbitmq.client.AMQP;
import io.vertx.core.Vertx;
import io.vertx.rabbitmq.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yhw
 * @date 2022-05-30
 */
@Slf4j
public class VertxRabbitClient extends BaseClient<VertxRabbitMessage> implements IRabbitClient<VertxRabbitMessage> {

    private RabbitMQClient client;

    private RabbitMQPublisher publisher;

    private RabbitConfig config;


    private Map<String,RabbitMQConsumer> consumerMap;

    public VertxRabbitClient(RabbitConfig config, Vertx vertx) {
        this.consumerMap = new ConcurrentHashMap<>();
        this.config = config;
        init(vertx);
    }

    private void init(Vertx vertx) {
        RabbitMQOptions options = new RabbitMQOptions();
        options.setHost(this.config.getHost());
        options.setPort(this.config.getPort());
        options.setUser(this.config.getUser());
        options.setPassword(this.config.getPassword());
        if (StrUtil.isNotBlank(this.config.getVirtualHost())) {
            options.setVirtualHost(this.config.getVirtualHost());
        }
        if (this.config.getConnectionTimeout() != null) {
            options.setConnectionTimeout(this.config.getConnectionTimeout());
        }
        if (this.config.getRequestedHeartbeat() != null) {
            options.setRequestedHeartbeat(this.config.getRequestedHeartbeat());
        }
        if (this.config.getHandshakeTimeout() != null) {
            options.setHandshakeTimeout(this.config.getHandshakeTimeout());
        }
        if (this.config.getRequestedChannelMax() != null) {
            options.setRequestedChannelMax(this.config.getRequestedChannelMax());
        }
        if (this.config.getNetworkRecoveryInterval() != null) {
            options.setNetworkRecoveryInterval(this.config.getNetworkRecoveryInterval());
        }
        if (StrUtil.isNotBlank(this.config.getConnectionName())) {
            options.setConnectionName(this.config.getConnectionName());
        }
        if (this.config.getAutomaticRecoveryEnabled() != null) {
            options.setAutomaticRecoveryEnabled(this.config.getAutomaticRecoveryEnabled());
        }

        if (this.config.getAutomaticRecoveryOnInitialConnection() != null) {
            options.setAutomaticRecoveryOnInitialConnection(this.config.getAutomaticRecoveryOnInitialConnection());
        }
        if (this.config.getIncludeProperties() != null) {
            options.setIncludeProperties(this.config.getIncludeProperties());
        }
        if (this.config.getUseNio() != null) {
            options.setUseNio(this.config.getUseNio());
        }

        //
        this.client = RabbitMQClient.create(vertx,options);
        this.client.start(r -> {
            log.info("connection status : {} ",r.succeeded());
        });

        //
        if (this.config.getHasReliablePublishing() != null && this.config.getHasReliablePublishing()) {
            this.publisher = RabbitMQPublisher.create(vertx, client, new RabbitMQPublisherOptions());
        }
    }


    @Override
    public boolean publish(String exchange, String routingKey, VertxRabbitMessage message) {
        if (!this.isAvailable()) {
            log.error("connection fail");
            return false;
        }
        if (this.publisher != null) {
            publisher.publish(exchange,routingKey, new AMQP.BasicProperties().builder().messageId(message.getMessageId()).build(),
                    message.getPayload());
            publisher.getConfirmationStream().handler(conf -> {
                if (conf.isSucceeded()) {
                    log.info("message : {} publish success",conf.getMessageId());
                } else {
                    log.error("message : {} publish fail",conf.getMessageId());
                }
            });
        } else {
            client.basicPublish(exchange,routingKey,message.getPayload());
        }
        return true;
    }

    @Override
    public boolean subscribe(String queue) {
        if (!this.isAvailable()) {
            log.error("connection fail");
            return false;
        }
        Set<ISubscribeHandler<VertxRabbitMessage>> subscribeHandlers = getHandlerMap().getOrDefault(queue,new HashSet<>())
                .stream().sorted(Comparator.comparing(ISubscribeHandler::getSort)).collect(Collectors.toSet());
        if (CollUtil.isEmpty(subscribeHandlers)) {
            log.error("handlers is empty");
            return false;
        }
        QueueOptions queueOptions = new QueueOptions().setAutoAck(this.config.getHasAutoAck());
        client.basicConsumer(queue, queueOptions, consumeResult -> {
            if (consumeResult.succeeded()) {
               log.info("RabbitMQ consumer : {}  created !",queue);
                RabbitMQConsumer consumer = consumeResult.result();
                consumerMap.put(queue,consumer);
                // Set the handler which messages will be sent to
                consumer.handler(msg -> {
                    // manual ack
                    if (!this.config.getHasAutoAck()) {
                        client.basicAck( msg.envelope().getDeliveryTag(), false);
                    }
                    // business
                    VertxRabbitMessage vertxRabbitMessage =  new VertxRabbitMessage(msg.properties().getMessageId(),msg.body());
                    for (ISubscribeHandler<VertxRabbitMessage> handler : subscribeHandlers) {
                        if (handler.handler(vertxRabbitMessage)) {
                            break;
                        }
                    }
                });
            } else {
                log.error("consumer created fail : ",consumeResult.cause());
            }
        });
        return true;
    }

    @Override
    public boolean unsubscribe(String queue) {
        RabbitMQConsumer rabbitMQConsumer = consumerMap.get(queue);
        if (rabbitMQConsumer == null) {
            return false;
        }
        rabbitMQConsumer.cancel(r -> {
            if (r.succeeded()) {
                log.info("cancel consumer : {}  success ", queue);
                consumerMap.remove(queue);
            } else {
                log.error("cancel consumer : {} fail : {}", queue,r.cause().getMessage());
            }
        });
        return true;
    }

    @Override
    public boolean isAvailable() {
        return this.client.isConnected();
    }

    @Override
    public boolean close() {
        this.client.stop(r -> {
            if (r.succeeded()) {
                log.info("stop client success ");
            } else {
                log.error("stop client fail : ",r.cause());
            }
        });
        return true;
    }
}
