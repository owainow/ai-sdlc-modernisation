package com.sourcegraph.demo.reporting.event;

import com.sourcegraph.demo.reporting.entity.BillingReadModel;
import com.sourcegraph.demo.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component
public class BillingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BillingEventConsumer.class);
    private final ReportRepository reportRepository;

    public BillingEventConsumer(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional
    public void handleHourCreated(Map<String, Object> event) {
        log.info("Handling hour.created event: {}", event);
        BillingReadModel model = new BillingReadModel();
        model.setBillableHourId(UUID.fromString((String) event.get("id")));
        model.setUserId(UUID.fromString((String) event.get("userId")));
        model.setCustomerId(UUID.fromString((String) event.get("customerId")));
        model.setCategoryId(UUID.fromString((String) event.get("categoryId")));
        model.setHours(new BigDecimal(event.get("hours").toString()));
        model.setWorkDate(LocalDate.parse((String) event.get("workDate")));
        model.setUpdatedAt(Instant.now());
        reportRepository.save(model);
    }

    @Transactional
    public void handleHourUpdated(Map<String, Object> event) {
        log.info("Handling hour.updated event: {}", event);
        UUID billableHourId = UUID.fromString((String) event.get("id"));
        reportRepository.findByBillableHourId(billableHourId).ifPresent(model -> {
            model.setUserId(UUID.fromString((String) event.get("userId")));
            model.setCustomerId(UUID.fromString((String) event.get("customerId")));
            model.setCategoryId(UUID.fromString((String) event.get("categoryId")));
            model.setHours(new BigDecimal(event.get("hours").toString()));
            model.setWorkDate(LocalDate.parse((String) event.get("workDate")));
            model.setUpdatedAt(Instant.now());
            reportRepository.save(model);
        });
    }

    @Transactional
    public void handleHourDeleted(Map<String, Object> event) {
        log.info("Handling hour.deleted event: {}", event);
        UUID billableHourId = UUID.fromString((String) event.get("id"));
        reportRepository.deleteByBillableHourId(billableHourId);
    }
}
