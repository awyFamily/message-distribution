package com.awyFamily.message.distribution.core.network;

import java.util.Collection;

/**
 * @author yhw
 * @date 2022-05-30
 */
public interface IBaseClient<T> {

    boolean addSubscribeHandler(String topic, ISubscribeHandler<T> handler);

    boolean addSubscribeHandler(String topic, Collection<ISubscribeHandler<T>> handlers);

    boolean isAvailable();

    boolean close();


}
