package com.adidas.challenge.subscription.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-notifications")
public class KafkaNotificationsConfig {
    private Boolean enabled = false;
    private String outputTopic;

}
