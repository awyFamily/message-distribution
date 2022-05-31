package com.awyFamily.message.distribution.core.sender.http;

import com.awyFamily.message.distribution.core.model.sender.SenderConfig;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.sender.ISender;
import com.awyFamily.message.distribution.core.sender.ISenderProvider;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的http提供者
 * @author yhw
 * @date 2021-10-19
 */
public class SimpleHttpSenderProvider implements ISenderProvider<SimpleHttpSender> {

    //按主题,缓存资源(避免重复创建相同资源)
    private final Map<String, ISender> topicRepository = new ConcurrentHashMap<>();

    @Override
    public SenderTypeEnum getSenderType() {
        return SenderTypeEnum.http;
    }

    /**
     * 不同的策略，不同实现,可以使用缓存
     * @param config 创建所需的配置信息
     * @return
     */
    @Override
    public ISender createSender(SenderConfig config) {
        //先从缓存中获取
        ISender sender = topicRepository.get(config.getTopic());
        //如果缓存中没有,则新建，并加入缓存
        //如果初始化慢,则加入缓存机制
        if (sender == null)
            sender = new SimpleHttpSender(config.getTopic());
            topicRepository.put(config.getTopic(),sender);
        return sender;
    }

    @Override
    public Mono<Void> removeSender(String topic) {
        topicRepository.remove(topic);
       return Mono.empty();
    }

}
