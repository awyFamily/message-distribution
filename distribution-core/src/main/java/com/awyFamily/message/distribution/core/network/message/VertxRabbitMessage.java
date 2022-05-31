package com.awyFamily.message.distribution.core.network.message;

import cn.hutool.core.lang.UUID;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

/**
 * @author yhw
 * @date 2022-05-31
 */
@Data
public class VertxRabbitMessage extends BaseVertxMessage<String> {

    public VertxRabbitMessage(Buffer payload) {
        super(UUID.randomUUID().toString(true),payload);
    }

    public VertxRabbitMessage(String messageId, Buffer payload) {
        super(messageId, payload);
    }

}
