package com.awyFamily.message.distribution.core.model.subscriber;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yhw
 * @date 2022-05-26
 */
@Getter
@AllArgsConstructor
public enum SubscriberTypeEnum {

    mqtt("mqtt",""),
    rabbit_mq("rabbit_mq",""),
    rocket_mq("rocket_mq",""),
    kafka("kafka",""),
    pulsar("pulsar",""),
    ;

    private String name;

    private String description;

    private static Map<String, SubscriberTypeEnum> repository;

    static {
        repository = new HashMap<>();
        for (SubscriberTypeEnum senderTypeEnum : SubscriberTypeEnum.values()) {
            repository.put(senderTypeEnum.getName(),senderTypeEnum);
        }
    }

    public static SubscriberTypeEnum getName(String name){
        return repository.get(name);
    }
}
