package com.awyFamily.message.distribution.core.sender;

import com.awyFamily.message.distribution.core.model.sender.MessagePayload;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 发送者
 * @author yhw
 * @date 2021-10-19
 * @param <T>
 */
public interface ISender<T extends Map> {

    /**
     * 发送主题标识(唯一标识)
     * @return 唯一标识
     */
    String getTopic();

    /**
     * 发送消息
     * @param payload payload
     */
    Mono<Void> send(MessagePayload<T> payload);


    /**
     * 提供者类型
     * @return 提供者类型
     */
    SenderTypeEnum getType();


    default String getSenderTypeName() {
        return this.getType().getName();
    }
}
