package com.awyFamily.message.distribution.core.sender.rabbit;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.model.sender.MessagePayload;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.network.message.VertxRabbitMessage;
import com.awyFamily.message.distribution.core.network.rabbit.IRabbitClient;
import com.awyFamily.message.distribution.core.sender.ISender;
import io.vertx.core.buffer.Buffer;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * @author yhw
 * @date 2022-05-31
 */
public class SimpleRabbitSender implements ISender<HashMap> {

    private IRabbitClient client;

    private String exchange;

    private String routingKey;

    public String getRoutingKey() {
        return StrUtil.isNotBlank(this.routingKey) ? this.routingKey : "";
    }

    public SimpleRabbitSender(IRabbitClient rabbitClient, String exchange, String routingKey) {
        this.client = rabbitClient;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public String getTopic() {
        return this.exchange + getRoutingKey();
    }

    @Override
    public Mono<Void> send(MessagePayload<HashMap> payload) {
        VertxRabbitMessage mqMsg = new VertxRabbitMessage(Buffer.buffer(JSONUtil.toJsonStr(payload.getPayloadMessage())));
        client.publish(exchange,getRoutingKey(), mqMsg);
        return Mono.empty();
    }

    @Override
    public SenderTypeEnum getType() {
        return SenderTypeEnum.rabbit_mq;
    }
}
