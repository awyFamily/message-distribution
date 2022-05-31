package com.awyFamily.message.distribution.core.network.message;

import io.vertx.core.buffer.Buffer;
import lombok.Data;

/**
 * @author yhw
 * @date 2022-05-27
 */
@Data
public class VertxMqttMessage extends BaseVertxMessage<Integer> {

    private  int qosLevel;
    private  boolean isDup;
    private  boolean isRetain;
    private final String topicName;

    public VertxMqttMessage(String topicName, Buffer payload) {
        super(payload);
        this.topicName = topicName;
    }

    public VertxMqttMessage(int messageId, int qosLevel, String topicName, Buffer payload) {
        super(messageId,payload);
        this.qosLevel = qosLevel;
        this.topicName = topicName;
    }
}
