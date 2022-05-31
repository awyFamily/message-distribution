package com.awyFamily.message.distribution.core.network.mqtt;

import lombok.Data;

@Data
public class MqttConfig {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String clientId;
    private int qos = 1;
    private Integer connectionTimeout;
    private Integer keepAliveInterval;


    public String getServerUri(){
        return "tcp://"
                + getHost()
                + ":"
                + getPort();
    }
}
