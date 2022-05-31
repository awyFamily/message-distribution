package com.awyFamily.message.distribution.core.network.rabbit;

import com.awyFamily.message.distribution.core.network.IBaseClient;

/**
 * @author yhw
 * @date 2022-05-30
 */
public interface IRabbitClient<T> extends IBaseClient<T> {

    boolean publish(String exchange,String routingKey,T message);

    boolean subscribe(String queue);

    boolean unsubscribe(String queue);

}
