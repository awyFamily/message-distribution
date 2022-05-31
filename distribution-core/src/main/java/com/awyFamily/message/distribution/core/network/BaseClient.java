package com.awyFamily.message.distribution.core.network;

import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yhw
 * @date 2022-05-31
 */
public abstract class BaseClient<T> implements IBaseClient<T> {

    @Getter
    private Map<String, Set<ISubscribeHandler<T>>> handlerMap;

    public BaseClient() {
        this.handlerMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean addSubscribeHandler(String topic, ISubscribeHandler<T> handler) {
        Set<ISubscribeHandler<T>> subscribeHandlers = handlerMap.getOrDefault(topic, new HashSet<>());
        subscribeHandlers.add(handler);
        handlerMap.put(topic,subscribeHandlers);
        return true;
    }

    @Override
    public boolean addSubscribeHandler(String topic, Collection<ISubscribeHandler<T>> handlers) {
        Set<ISubscribeHandler<T>> subscribeHandlers = handlerMap.getOrDefault(topic, new HashSet<>());
        subscribeHandlers.addAll(handlers);
        handlerMap.put(topic,subscribeHandlers);
        return true;
    }
}
