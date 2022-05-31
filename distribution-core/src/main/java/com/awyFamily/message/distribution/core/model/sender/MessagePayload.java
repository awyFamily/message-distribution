package com.awyFamily.message.distribution.core.model.sender;

import cn.hutool.core.lang.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author yhw
 * @date 2021-10-15
 * @param <T>
 */
@NoArgsConstructor
@Data
public class MessagePayload<T extends Map> {

    /**
     * 唯一标识
     */
    private String id;

    /**
     * 是否只发送 message.data 数据
     */
    private boolean sendData;

    private Message<T> message;

    public Object getPayloadMessage() {
        if (this.sendData) {
            return this.message.getData();
        }
        return this.message;
    }

    public MessagePayload(T data) {
        this(new Message(data),false);
    }

    public MessagePayload(T data,boolean sendData) {
        this(new Message(data),sendData);
    }

    public MessagePayload(Message message,boolean sendData) {
        this(UUID.randomUUID().toString(),sendData,message);
    }

    public MessagePayload(String id,boolean sendData,Message<T> message) {
        this.id = id;
        this.message = message;
        this.sendData = sendData;
    }
}
