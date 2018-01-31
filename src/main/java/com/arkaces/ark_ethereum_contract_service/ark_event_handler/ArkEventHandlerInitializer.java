package com.arkaces.ark_ethereum_contract_service.ark_event_handler;

import com.arkaces.ApiException;
import com.arkaces.aces_listener_api.AcesListenerApi;
import com.arkaces.ark_ethereum_contract_service.ServiceArkAccountSettings;
import io.swagger.client.model.Subscription;
import io.swagger.client.model.SubscriptionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ArkEventHandlerInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final AcesListenerApi arkListener;
    private final String arkEventCallbackUrl;
    private final Integer arkMinConfirmations;
    private final ServiceArkAccountSettings serviceArkAccountSettings;

    private String subscriptionId;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("Creating ark listener subscription on " + serviceArkAccountSettings.getAddress());

        // Subscribe to ark listener on service ark address
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setCallbackUrl(arkEventCallbackUrl);
        subscriptionRequest.setMinConfirmations(arkMinConfirmations);
        subscriptionRequest.setRecipientAddress(serviceArkAccountSettings.getAddress());

        Subscription subscription;
        try {
            subscription = arkListener.subscriptionsPost(subscriptionRequest);
        } catch (ApiException e) {
            throw new RuntimeException("Ark Listener subscription failed to POST", e);
        }

        log.info("Created ark listener subscription " + subscriptionId);
        subscriptionId = subscription.getId();
    }

    @PreDestroy
    public void onDestroy() {
        log.info("Unsubscribing ark listener subscription " + subscriptionId);
        try {
            arkListener.subscriptionsIdUnsubscribesPost(subscriptionId);
        } catch (ApiException e) {
            throw new RuntimeException("Ark Listener unsubscribe failed to POST", e);
        }
        log.info("Successfully unsubscribed ark listener subscription " + subscriptionId);
    }
}
