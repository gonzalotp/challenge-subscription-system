package com.adidas.challenge.emailservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "email")
@Data
public class EmailConfig {
    private String inputTopic;
    private SmtpConfig smtp;
    private DlqConfig dlq;

    @Data
    public static class SmtpConfig {
        private String server;
        private String user;
        private String password;
    }

    @Data
    public static class DlqConfig {
        private Boolean enabled = false;
        private String outputTopic;
    }
}
