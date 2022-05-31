package com.awyFamily.message.distribution.core.manager;

import cn.hutool.core.collection.CollUtil;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberConfig;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import com.awyFamily.message.distribution.core.subscriber.ISubscriber;
import com.awyFamily.message.distribution.core.subscriber.ISubscriberProvider;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yhw
 * @date 2022-05-27
 */
public class SimpleSubscriberManager implements ISubscriberManager<MqttMessage> {

    private static Map<String, ISubscriberProvider> providerMap = new ConcurrentHashMap<>();

    //发送者配置列表
    private final Map<String, SubscriberConfig> configMap;

    public SimpleSubscriberManager(List<SubscriberConfig> configs, List<ISubscriberProvider> providers) {
        this.configMap = new ConcurrentHashMap<>();
        if (CollUtil.isNotEmpty(configs)) {
            for (SubscriberConfig config : configs) {
                configMap.put(config.getId(),config);
            }
        }
        register(providers);
    }

    private void register(List<ISubscriberProvider> providers) {
        for (ISubscriberProvider provider : providers) {
            providerMap.put(provider.getSubscriberTypeName(),provider);
        }
    }

    @Override
    public ISubscriber getSubscriber(String id, Collection<ISubscribeHandler<MqttMessage>> iSubscribeHandlers) {
        SubscriberConfig config = configMap.get(id);
        if (config != null) {
            ISubscriberProvider provider = providerMap.get(config.getType().getName());
            if (provider != null) {
                return provider.createSubscriber(config,iSubscribeHandlers);
            }
        }
        return null;
    }

    @Override
    public void remove(String id) {
        SubscriberConfig config = configMap.get(id);
        if (config != null) {
            ISubscriberProvider provider = providerMap.get(config.getType().getName());
            if (provider != null) {
                provider.removeSubscriber(config.getTopic()).subscribe();
            }
        }
    }
}
