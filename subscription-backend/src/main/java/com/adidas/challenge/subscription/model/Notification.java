package com.adidas.challenge.subscription.model;

import lombok.Data;

@Data
public class Notification {

    private Type type;
    private Subscription subscription;

    public Notification() {

    }

    public Notification(Type type, Subscription subscription) {
        this.type = type;
        this.subscription = subscription;
    }

    public enum Type {
        SUBSCRIPTION_CREATION,
        SUBSCRIPTION_DELETION
    }
}
