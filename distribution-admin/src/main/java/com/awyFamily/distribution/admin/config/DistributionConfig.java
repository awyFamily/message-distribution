package com.awyFamily.distribution.admin.config;

import cn.hutool.json.JSONUtil;
import com.awyFamily.message.distribution.core.manager.ISenderManager;
import com.awyFamily.message.distribution.core.manager.ISubscriberManager;
import com.awyFamily.message.distribution.core.manager.SimpleSenderProviderManager;
import com.awyFamily.message.distribution.core.manager.SimpleSubscriberManager;
import com.awyFamily.message.distribution.core.model.sender.SenderConfig;
import com.awyFamily.message.distribution.core.model.sender.SenderTypeEnum;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberConfig;
import com.awyFamily.message.distribution.core.model.subscriber.SubscriberTypeEnum;
import com.awyFamily.message.distribution.core.network.mqtt.MqttConfig;
import com.awyFamily.message.distribution.core.network.rabbit.RabbitConfig;
import com.awyFamily.message.distribution.core.sender.ISenderProvider;
import com.awyFamily.message.distribution.core.sender.http.SimpleHttpSenderProvider;
import com.awyFamily.message.distribution.core.sender.rabbit.SimpleRabbitSenderProvider;
import com.awyFamily.message.distribution.core.subscriber.ISubscriberProvider;
import com.awyFamily.message.distribution.core.subscriber.mqtt.SimpleMqttSubscriberProvider;
import com.awyFamily.message.distribution.core.subscriber.rabbit.SimpleRabbitSubscriberProvider;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yhw
 * @date 2022-05-27
 */
@Configuration
public class DistributionConfig {

    @Bean
    public ISenderManager simpleSenderProviderManager() {
        List<SenderConfig> configs = new ArrayList<>();
        SenderConfig config = new SenderConfig();
        config.setType(SenderTypeEnum.http);
        config.setId("http");
        config.setTopic("127.0.0.1:8080/callback");
        configs.add(config);

        config = new SenderConfig();
        config.setContent(JSONUtil.toJsonStr(getRabbitConfig()));

        config.setType(SenderTypeEnum.rabbit_mq);
        config.setId("rabbit");
        config.setTopic("xxxx");
        configs.add(config);
        Map<String, List<SenderConfig>> configMap = configs.stream().collect(Collectors.groupingBy(SenderConfig::getId));

        List<ISenderProvider > providers = new ArrayList<>();
        providers.add(new SimpleHttpSenderProvider());
        providers.add(new SimpleRabbitSenderProvider());

        return new SimpleSenderProviderManager(configMap,providers);
    }

    @Bean
    public ISubscriberManager<MqttMessage> simpleSubscriberManager() {
        List<SubscriberConfig> configs = new ArrayList<>();
        SubscriberConfig config = new SubscriberConfig();
        config.setId("1");
        config.setTopic("xxxx");
        config.setType(SubscriberTypeEnum.mqtt);
        config.setContent(JSONUtil.toJsonStr(getMqttConfig()));

        configs.add(config);
        config = new SubscriberConfig();
        config.setId("2");
        config.setTopic("xxx");
        config.setType(SubscriberTypeEnum.mqtt);
        config.setContent(JSONUtil.toJsonStr(getMqttConfig()));
        configs.add(config);

        //
        config = new SubscriberConfig();
        config.setId("3");
        config.setTopic("xxx");
        config.setType(SubscriberTypeEnum.rabbit_mq);
        config.setContent(JSONUtil.toJsonStr(getRabbitConfig()));
        configs.add(config);

        List<ISubscriberProvider > providers = new ArrayList<>();
        providers.add(new SimpleMqttSubscriberProvider());
        providers.add(new SimpleRabbitSubscriberProvider());
        return new SimpleSubscriberManager(configs,providers);
    }

    private RabbitConfig getRabbitConfig() {
        RabbitConfig rabbitConfig = new RabbitConfig();
        rabbitConfig.setHasReliablePublishing(true);
        rabbitConfig.setUser("xxx");
        rabbitConfig.setPassword("xxxx");
        rabbitConfig.setHost("xxxxx");
        rabbitConfig.setPort(5672);
        return rabbitConfig;
    }

    private MqttConfig getMqttConfig() {
        MqttConfig mqttConfig = new MqttConfig();
        mqttConfig.setClientId("xxx");
        mqttConfig.setUsername("xxxx");
        mqttConfig.setPassword("xxxx");
        mqttConfig.setKeepAliveInterval(3);
        mqttConfig.setConnectionTimeout(30);
        mqttConfig.setKeepAliveInterval(30);
        mqttConfig.setQos(0);
        mqttConfig.setHost("xxxx");
        mqttConfig.setPort(1883);
        return mqttConfig;
    }
}
