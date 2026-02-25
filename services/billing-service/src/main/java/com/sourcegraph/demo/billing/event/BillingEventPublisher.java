package com.sourcegraph.demo.billing.event;

import com.sourcegraph.demo.billing.entity.BillableHour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BillingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BillingEventPublisher.class);
    private static final String TOPIC = "billing-events";

    public void publishHourCreated(BillableHour hour) {
        log.info("Publishing hour.created event for id={} to topic={}", hour.getId(), TOPIC);
    }

    public void publishHourUpdated(BillableHour hour) {
        log.info("Publishing hour.updated event for id={} to topic={}", hour.getId(), TOPIC);
    }

    public void publishHourDeleted(UUID id) {
        log.info("Publishing hour.deleted event for id={} to topic={}", id, TOPIC);
    }
}
