package com.adidas.challenge.emailservice.consumer;

import com.adidas.challenge.emailservice.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.LoggingErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.util.Objects;

@Slf4j
public class DeadLetterQueueErrorHandler extends LoggingErrorHandler {
    @Autowired
    private EmailConfig config;
    @Autowired
    private KafkaTemplate<Object, byte[]> kafkaTemplate;

    @Override
    public void handle(@NotNull Exception thrownException, @NotNull ConsumerRecord<?, ?> record) {
        super.handle(thrownException, record);
        Throwable cause = thrownException.getCause();
        if (cause instanceof DeserializationException
                && Objects.nonNull(config.getDlq()) && config.getDlq().getEnabled()) {
            log.warn("Deserialization exception. Sending record into Dead Letter Queue topic.");
            byte[] originalPayload = ((DeserializationException) cause).getData();
            kafkaTemplate.send(config.getDlq().getOutputTopic(), originalPayload);
        }
    }
}
