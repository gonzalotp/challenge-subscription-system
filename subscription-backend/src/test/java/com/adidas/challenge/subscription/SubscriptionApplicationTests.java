package com.adidas.challenge.subscription;


import com.adidas.challenge.subscription.controller.SubscriptionController;
import com.adidas.challenge.subscription.model.Notification;
import com.adidas.challenge.subscription.model.Subscription;
import com.adidas.challenge.subscription.repository.SubscriptionRepository;
import com.adidas.challenge.subscription.service.KafkaNotificationsService;
import com.adidas.challenge.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionApplicationTests {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SubscriptionController subscriptionController;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    @InjectMocks
    private SubscriptionService subscriptionService;
    @Mock
    private KafkaNotificationsService kafkaNotificationsService;

    private final Subscription validSub = new Subscription(null, "test@email.com", "MyName", "M",
            Date.from(Instant.now().minus(Duration.ofDays(1L))), true, "1234");
    private final Subscription subWithoutEmail = new Subscription(null, null, "MyName", "M",
            Date.from(Instant.now().minus(Duration.ofDays(1L))), true, "1234");
    private final Subscription subWithoutConsent = new Subscription(null, "test3@email.fake", "MyName", "M",
            Date.from(Instant.now().minus(Duration.ofDays(1L))), null, "1234");
    private final Subscription subWithoutCampaignId = new Subscription(null, "test3@email.fake", "MyName", "M",
            Date.from(Instant.now().minus(Duration.ofDays(1L))), true, "");

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        Mockito.doNothing().when(kafkaNotificationsService).send(new Notification());
    }

    @AfterEach
    void teardown() {
        subscriptionRepository.deleteAll();
    }

    @Test
    void contextLoadsTest() {
        assertThat(subscriptionController).isNotNull();
    }

    @Test
    void emptySubscriptionListTest() throws Exception {
        mockMvc.perform(get("/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void addValidSubscriptionTest() throws Exception {
        final String jsonBody = objectMapper.writeValueAsString(validSub);

        final MvcResult postResult = mockMvc.perform(post("/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isCreated())
                .andReturn();
        final Subscription createdSub = objectMapper.readValue(postResult.getResponse().getContentAsByteArray(),
                Subscription.class);
        final Long id = createdSub.getId();
        final String location = postResult.getResponse().getHeader("Location");

        assertThat(id).isNotNull();
        assertThat(location).isNotNull();
        assertThat(location).isEqualTo("/subscriptions/" + id);

        final MvcResult getResult = mockMvc.perform(get(location)).andReturn();
        final Subscription getSub = objectMapper.readValue(getResult.getResponse().getContentAsByteArray(), Subscription.class);

        assertThat(getSub).isEqualTo(createdSub);
    }

    @Test
    void addInvalidSubscriptionTest() throws Exception {
        final String[] wrongJsonBodies = {objectMapper.writeValueAsString(subWithoutEmail),
                objectMapper.writeValueAsString(subWithoutConsent),
                objectMapper.writeValueAsString(subWithoutCampaignId)};

        for (String jsonBody : wrongJsonBodies) {
            mockMvc.perform(post("/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void subscriptionIdDoesNotRepeatTest() throws Exception {
        final Subscription validSub2 = new Subscription(null, "test2@email.com", "MyName",
                "M", Date.from(Instant.now().minus(Duration.ofDays(1L))), true, "1234");

        final Subscription sub1 = createSubscription(validSub);

        mockMvc.perform(delete("/subscriptions/" + sub1.getId()))
                .andExpect(status().isNoContent());

        final Subscription sub2 = createSubscription(validSub2);

        assertThat(sub1.getId()).isNotEqualTo(sub2.getId());
    }

    @Test
    void addDuplicateSubscriptionTest() throws Exception {
        final String jsonSub = objectMapper.writeValueAsString(validSub);
        createSubscription(validSub);
        mockMvc.perform(post("/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSub))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteSubscriptionTest() throws Exception {
        final Subscription sub = createSubscription(validSub);
        mockMvc.perform(delete("/subscriptions/" + sub.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/subscriptions" + sub.getId()))
                .andExpect(status().isNotFound());
    }

    private Subscription getSubscriptionFromResult(MvcResult result) throws IOException {
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Subscription.class);
    }

    private Subscription createSubscription(Subscription subscription) throws Exception {
        final String jsonSub = objectMapper.writeValueAsString(subscription);

        MvcResult postResult = mockMvc.perform(post("/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSub))
                .andExpect(status().isCreated())
                .andReturn();

        return getSubscriptionFromResult(postResult);
    }
}
