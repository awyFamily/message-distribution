package com.awyFamily.message.distribution.core.model.sender;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yhw
 * @date 2021-10-15
 */
@Getter
@AllArgsConstructor
public enum SenderTypeEnum {

    http("http",""),
    mqtt("mqtt",""),
    rabbit_mq("rabbit_mq",""),
    rocket_mq("rocket_mq",""),
    kafka("kafka",""),
    pulsar("pulsar",""),
    ;

    private String name;

    private String description;

    private static Map<String, SenderTypeEnum> repository;

    static {
        repository = new HashMap<>();
        for (SenderTypeEnum senderTypeEnum : SenderTypeEnum.values()) {
            repository.put(senderTypeEnum.getName(),senderTypeEnum);
        }
    }

    public static SenderTypeEnum getName(String name){
        return repository.get(name);
    }
}
