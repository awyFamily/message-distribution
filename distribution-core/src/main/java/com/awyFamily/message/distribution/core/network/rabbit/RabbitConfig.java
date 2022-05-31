package com.awyFamily.message.distribution.core.network.rabbit;

import lombok.Data;

/**
 * @author yhw
 * @date 2022-05-30
 */
@Data
public class RabbitConfig {

    private String user;
    private String password;
    private String host;
    private String virtualHost;
    private int port;
    private Integer connectionTimeout;
    private Integer requestedHeartbeat;
    private Integer handshakeTimeout;
    private Integer requestedChannelMax;
    private Long networkRecoveryInterval;
    private Boolean automaticRecoveryEnabled;
    private Boolean automaticRecoveryOnInitialConnection;
    private Boolean includeProperties;
    private Boolean useNio;
    private String connectionName;
    //
    private Boolean hasReliablePublishing;
    private Boolean hasAutoAck = true;

    //
    private String routingKey;


    public String getClientId() {
        return this.host + this.port + this.user;
    }

}
