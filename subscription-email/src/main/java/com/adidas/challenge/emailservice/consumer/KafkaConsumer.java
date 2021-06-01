package com.adidas.challenge.emailservice.consumer;

import com.adidas.challenge.emailservice.model.Notification;
import com.adidas.challenge.emailservice.service.EmailSender;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class KafkaConsumer {

    @Getter
    @Setter
    private CountDownLatch latch = new CountDownLatch(1);

    @Autowired
    private EmailSender emailSender;

    @KafkaListener(topics = "${email.inputTopic}")
    public void consumeNotification(Notification notification) {
        latch.countDown();
        emailSender.send(notification);
    }

    @Bean
    public DeadLetterQueueErrorHandler errorHandler() {
        return new DeadLetterQueueErrorHandler();
    }
}
