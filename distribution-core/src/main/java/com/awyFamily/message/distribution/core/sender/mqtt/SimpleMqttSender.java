package com.awyFamily.message.distribution.core.sender.mqtt;

import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.model.sender.MessagePayload;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.network.mqtt.IMqttClient;
import com.awyFamily.message.distribution.core.network.mqtt.PahoMqttClient;
import com.awyFamily.message.distribution.core.sender.ISender;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * @author yhw
 * @date 2021-10-19
 */
@Slf4j
public class SimpleMqttSender implements ISender<HashMap> {

    private final String topic;

    private IMqttClient client;

    public SimpleMqttSender(String topic,IMqttClient client) {
        this.topic = topic;
        this.client = client;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public Mono<Void> send(MessagePayload<HashMap> payload) {
        MqttMessage mqMsg = new MqttMessage(JSONUtil.toJsonStr(payload.getPayloadMessage()).getBytes());
        client.publish(topic, mqMsg);
        return Mono.empty();
    }


    @Override
    public SenderTypeEnum getType() {
        return SenderTypeEnum.http;
    }

}
