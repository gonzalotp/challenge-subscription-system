package com.adidas.challenge.subscription.service;

import com.adidas.challenge.subscription.config.KafkaNotificationsConfig;
import com.adidas.challenge.subscription.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationsService {
    @Autowired
    private KafkaNotificationsConfig kafkaNotificationsConfig;
    @Autowired
    private KafkaTemplate<Object, Notification> kafkaTemplate;

    public void send(Notification notification) {
        kafkaTemplate.send(kafkaNotificationsConfig.getOutputTopic(), notification);
    }
}
