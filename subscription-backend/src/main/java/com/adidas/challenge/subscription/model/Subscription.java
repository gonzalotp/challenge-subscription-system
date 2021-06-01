package com.adidas.challenge.subscription.model;

import lombok.Data;
import org.springframework.data.domain.Example;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

@Data
@Entity
@Table(name = TableCatalogue.SUBSCRIPTIONS_TABLE,
        uniqueConstraints = {@UniqueConstraint(columnNames = {"email", "campaignId"})})
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull(message = "email must not be null")
    @Email
    private String email;
    private String firstName;
    private String gender;
    @NotNull(message = "birthday must not be null")
    @Past
    private Date birthday;
    @NotNull(message = "consent must not be null")
    @AssertTrue(message = "consent must be true")
    private Boolean consent;
    @NotNull(message = "campaignId must not be null")
    @NotEmpty
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

    public static Example<Subscription> getExampleOf(Subscription subscription) {
        final Subscription exampleSub = new Subscription();
        exampleSub.setEmail(subscription.getEmail());
        exampleSub.setCampaignId(subscription.getCampaignId());
        return Example.of(exampleSub);
    }
}
