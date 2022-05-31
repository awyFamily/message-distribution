package com.awyFamily.message.distribution.core.model.sender;

import lombok.Data;

/**
 * @author yhw
 * @date 2021-10-15
 */
@Data
public class SenderConfig {

    /**
     * 资源唯一标识(唯一标识)
     */
    private String id;

    /**
     * 例如:
     * http 是 host
     */
    private String topic;

    /**
     * 配置类型
     * 例如：
     * http
     */
    private SenderTypeEnum type;

    /**
     * 配置内容明细
     * 例如：mqtt 配置
     * host: xxxx,
     * username: xxxx,
     * password: xxxx
     */
    private String content;

}
