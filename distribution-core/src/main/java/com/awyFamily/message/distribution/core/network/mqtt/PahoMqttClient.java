package com.awyFamily.message.distribution.core.network.mqtt;

import cn.hutool.core.collection.CollUtil;
import com.awyFamily.message.distribution.core.network.BaseClient;
import com.awyFamily.message.distribution.core.network.ISubscribeHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yhw
 * @date 2022-05-26
 */
@Slf4j
public class PahoMqttClient extends BaseClient<MqttMessage> implements IMqttClient<MqttMessage> {

    private MqttClient client = null;

    private MqttConfig config;

    private int retrySize;

    private boolean hasRetry;


    public PahoMqttClient(MqttConfig config) {
        this(config,3);
    }

    public PahoMqttClient(MqttConfig config,int retrySize) {
        this.retrySize = retrySize;
        this.hasRetry = false;
        this.config = config;
        initMq();
    }

    private void initMq() {
        String cid = config.getClientId();
        log.info("Init MQ {}", cid);
        try {
            this.client = new MqttClient(config.getServerUri(), config.getClientId(), new MemoryPersistence());
        } catch (MqttException e) {
            log.error("Mqtt setting error ", e);
            return;
        }

        var options = new MqttConnectOptions();

        // config connection info form config set
        options.setUserName(config.getUsername());
        options.setPassword(config.getPassword().toCharArray());
        options.setConnectionTimeout(config.getConnectionTimeout());
        options.setKeepAliveInterval(config.getKeepAliveInterval());


        options.setAutomaticReconnect(true);
        this.client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (hasRetry) {
                    hasRetry = false;
                    if (CollUtil.isNotEmpty(getHandlerMap())) {
                        for (String topic : getHandlerMap().keySet()) {
                            unsubscribe(topic);
                            subscribe(topic,config.getQos());
                        }
                    }
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                log.error("{} Connection to MQTT lost", cid, cause);
                retryConnection(retrySize);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                if (!token.isComplete()) {
                    log.warn("mqtt token {} is not completed", token.getMessageId());
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                log.info("topic : " + topic);
            }
        });

        try {
            this.client.connect(options);
        } catch (MqttException e) {
            log.error("Cannot connect to mqtt server", e);
            return;
        }
    }


    @Override
    public boolean publish(String topic, int qos, MqttMessage message) {
        if (!this.isAvailable()) {
            log.error("connection fail");
            return false;
        }
        try {
            message.setQos(qos);
            client.publish(topic, message);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean subscribe(String topic, int qos) {
        try {
            if (!this.isAvailable()) {
                retryConnection(retrySize);
            }
            if (this.isAvailable() && CollUtil.isNotEmpty(getHandlerMap().getOrDefault(topic,new HashSet<>()))) {
                Set<ISubscribeHandler<MqttMessage>> handlers = getHandlerMap().getOrDefault(topic,new HashSet<>())
                        .stream().sorted(Comparator.comparing(ISubscribeHandler::getSort)).collect(Collectors.toSet());

                client.subscribe(topic,qos,(top,message) -> {
                    for (ISubscribeHandler<MqttMessage> handler : handlers) {
                        if (handler.handler(message)) {
                            break;
                        }
                    }
                });
                return true;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean unsubscribe(String topic) {
        try {
            if (this.isAvailable()) {
                client.unsubscribe(topic);
                return true;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return client != null && client.isConnected();
    }

    @Override
    public boolean close() {
        try {
            if (isAvailable()) {
                client.disconnect();
                client.close();
                log.info("mqtt resource close");
                return true;
            }
        } catch (MqttException e) {
            e.printStackTrace();
            log.error("",e);
        }
        return false;
    }

    private void retryConnection(int retrySize){
        if(retrySize < 0){
            return;
        }
        try {
            log.error("mqtt reconnection ....");
            client.reconnect();
            int retryNumber = (this.retrySize - retrySize) + 1;
            log.error("retry number : {}",retryNumber);
            TimeUnit.SECONDS.sleep(30 * (retryNumber == 0 ? 1 : retryNumber));
            if(!client.isConnected()){
                retryConnection(--retrySize);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        hasRetry = true;
    }
}
