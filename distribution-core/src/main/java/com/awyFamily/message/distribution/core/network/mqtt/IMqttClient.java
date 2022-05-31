package com.awyFamily.message.distribution.core.network.mqtt;

import com.awyFamily.message.distribution.core.network.IBaseClient;

/**
 * @author yhw
 * @date 2022-05-26
 */
public interface IMqttClient<T> extends IBaseClient<T> {

    default boolean publish(String topic,T message) {
        return publish(topic,1,message);
    }

    boolean publish(String topic,int qos,T message);

    default boolean subscribe(String topic) {
        return subscribe(topic,1);
    }

    boolean subscribe(String topic, int qos);

    boolean unsubscribe(String topic);

}
