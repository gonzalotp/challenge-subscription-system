package com.adidas.challenge.emailservice;

import com.adidas.challenge.emailservice.config.EmailConfig;
import com.adidas.challenge.emailservice.consumer.KafkaConsumer;
import com.adidas.challenge.emailservice.model.Notification;
import com.adidas.challenge.emailservice.model.Subscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Date;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(topics = "${email.inputTopic}",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class EmailserviceApplicationTests {

    @Autowired
    private EmailConfig emailConfig;
    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;
    @Autowired
    private KafkaConsumer consumer;

    private final Notification validNotification = new Notification(Notification.Type.SUBSCRIPTION_CREATION,
            new Subscription(1L, "aaa@mail.com", "MyName", "m",
                    Date.from(Instant.now().minusSeconds(5)), true, "1234"));

    @BeforeEach
    void resetLatch() {
        consumer.setLatch(new CountDownLatch(1));
    }

    @Test
    void correctMessageTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        kafkaTemplate.send(emailConfig.getInputTopic(), objectMapper.writeValueAsBytes(validNotification));
        assert consumer.getLatch().await(30L, TimeUnit.SECONDS);
    }

    @Test
    void incorrectMessageTest() throws Exception {
        if (emailConfig.getDlq().getEnabled()) {
            ObjectMapper objectMapper = new ObjectMapper();
            kafkaTemplate.send(emailConfig.getInputTopic(), objectMapper.writeValueAsBytes(new Subscription()));
            assert consumer.getLatch().await(30L, TimeUnit.SECONDS);
        }
    }

}
