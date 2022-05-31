package com.awyFamily.message.distribution.core.sender;

import com.awyFamily.message.distribution.core.model.sender.SenderConfig;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import reactor.core.publisher.Mono;

/**
 * 发送提供者
 * @author yhw
 * @date 2021-10-15
 */
public interface ISenderProvider<T extends ISender> {

    /**
     * @return sender 类型
     */
    SenderTypeEnum getSenderType();


    default String getSenderTypeName() {
       return this.getSenderType().getName();
    }

    /**
     * 创建并获取 sender
     * @param config 创建所需的配置信息
     * @return
     */
    ISender createSender(SenderConfig config);


    Mono<Void> removeSender(String topic);

}
