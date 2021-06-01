package com.adidas.challenge.subscription.service;

import com.adidas.challenge.subscription.config.KafkaNotificationsConfig;
import com.adidas.challenge.subscription.model.Notification;
import com.adidas.challenge.subscription.model.Notification.Type;
import com.adidas.challenge.subscription.model.Subscription;
import com.adidas.challenge.subscription.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class SubscriptionService {

    @Autowired
    private KafkaNotificationsConfig kafkaNotificationsConfig;
    @Autowired
    private KafkaNotificationsService kafkaNotificationsService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public List<Subscription> getAllSubscriptions() {
        log.debug("Retrieving all subscriptions");
        return subscriptionRepository.findAll();
    }

    public Optional<Subscription> getSubscription(Long id) {
        log.debug("Retrieving subscription with id {}", id);
        return subscriptionRepository.findById(id);
    }

    public Subscription createSubscription(Subscription subscription) throws EntityExistsException {
        log.debug("Creating subscription for campaign: {}", subscription.getCampaignId());
        subscriptionRepository.findOne(Subscription.getExampleOf(subscription))
                .ifPresent(sub -> {
                    log.warn("Attempted creation of a new subscription that already exists. Id: {}, campaign: {}",
                            sub.getId(), sub.getCampaignId());
                    throw new EntityExistsException("This email is already subscribed to the campaign");
                });

        Subscription createdSubscription = subscriptionRepository.save(subscription);

        if (kafkaNotificationsConfig.getEnabled()) {
            Notification notification = new Notification(Type.SUBSCRIPTION_CREATION, createdSubscription);
            kafkaNotificationsService.send(notification);
        }

        return createdSubscription;
    }

    public void deleteSubscription(Long id) {
        log.debug("Deleting subscription with id: {}", id);
        subscriptionRepository.findById(id).ifPresent(subscription -> {
            subscriptionRepository.deleteById(id);
            if (kafkaNotificationsConfig.getEnabled()) {
                Notification notification = new Notification(Type.SUBSCRIPTION_DELETION, subscription);
                kafkaNotificationsService.send(notification);
            }
        });
    }
}
