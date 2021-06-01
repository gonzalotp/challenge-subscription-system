package com.adidas.challenge.emailservice.model;

import lombok.Data;

import java.util.Date;

@Data
public class Subscription {
    private Long id;
    private String email;
    private String firstName;
    private String gender;
    private Date birthday;
    private Boolean consent;
    private String campaignId;

    public Subscription() {
    }

    public Subscription(Long id, String email, String firstName, String gender, Date birthday, Boolean consent, String campaignId) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.gender = gender;
        this.birthday = birthday;
        this.consent = consent;
        this.campaignId = campaignId;
    }
}
