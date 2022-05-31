package com.awyFamily.message.distribution.core.network.message;

import io.vertx.core.buffer.Buffer;
import lombok.Data;

/**
 * @author yhw
 * @date 2022-05-31
 */
@Data
public class BaseVertxMessage<T> {

    private  T messageId;
    //消息正文
    private final Buffer payload;

    public BaseVertxMessage(Buffer payload) {
        this.payload = payload;
    }

    public BaseVertxMessage(T messageId, Buffer payload) {
        this.messageId = messageId;
        this.payload = payload;
    }
}
