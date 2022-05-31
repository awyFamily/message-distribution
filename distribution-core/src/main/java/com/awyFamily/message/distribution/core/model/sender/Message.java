package com.awyFamily.message.distribution.core.model.sender;

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
public class Message<T  extends Map> {

    /**
     * 消息版本
     */
    private String version;

    /**
     * 类型
     */
    private String type;

    /**
     * 命令码
     */
    private String cmd;

    /**
     * 拓展预留
     */
    private Map<String,Object> expends;

    /**
     * 数据体
     */
    private T data;

    public Message(T data) {
        this.data = data;
    }
}
