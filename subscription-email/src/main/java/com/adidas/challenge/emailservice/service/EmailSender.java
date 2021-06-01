package com.adidas.challenge.emailservice.service;

import com.adidas.challenge.emailservice.config.EmailConfig;
import com.adidas.challenge.emailservice.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailSender {

    @Autowired
    private EmailConfig config;

    public void send(Notification notification) {
        log.info("Sending " + notification.getType() + " email to " + notification.getSubscription());
        /* SMTP client sends the email */
    }
}
