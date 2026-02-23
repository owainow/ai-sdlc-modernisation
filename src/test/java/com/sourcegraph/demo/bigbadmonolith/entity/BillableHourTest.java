package com.sourcegraph.demo.bigbadmonolith.entity;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T011: Characterisation tests for BillableHour entity — constructor, getters, setters,
 * FK ID storage, Joda-Time dates.
 */
class BillableHourTest {

    @Test
    void defaultConstructorCreatesEmptyBillableHour() {
        BillableHour bh = new BillableHour();
        assertThat(bh.getId()).isNull();
        assertThat(bh.getCustomerId()).isNull();
        assertThat(bh.getUserId()).isNull();
        assertThat(bh.getCategoryId()).isNull();
        assertThat(bh.getHours()).isNull();
        assertThat(bh.getNote()).isNull();
        assertThat(bh.getDateLogged()).isNull();
        assertThat(bh.getCreatedAt()).isNull();
    }

    @Test
    void sixArgConstructorSetsFieldsAndCreatedAt() {
        LocalDate dateLogged = new LocalDate(2024, 6, 15);
        BillableHour bh = new BillableHour(1L, 2L, 3L, new BigDecimal("8.50"), "Dev work", dateLogged);

        assertThat(bh.getId()).isNull();
        assertThat(bh.getCustomerId()).isEqualTo(1L);
        assertThat(bh.getUserId()).isEqualTo(2L);
        assertThat(bh.getCategoryId()).isEqualTo(3L);
        assertThat(bh.getHours()).isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(bh.getNote()).isEqualTo("Dev work");
        assertThat(bh.getDateLogged()).isEqualTo(dateLogged);
        assertThat(bh.getCreatedAt()).isNotNull();
    }

    @Test
    void eightArgConstructorSetsAllFields() {
        LocalDate dateLogged = new LocalDate(2024, 3, 10);
        DateTime createdAt = new DateTime(2024, 3, 10, 9, 0, 0);
        BillableHour bh = new BillableHour(10L, 1L, 2L, 3L, new BigDecimal("4.25"), "Support", dateLogged, createdAt);

        assertThat(bh.getId()).isEqualTo(10L);
        assertThat(bh.getCustomerId()).isEqualTo(1L);
        assertThat(bh.getUserId()).isEqualTo(2L);
        assertThat(bh.getCategoryId()).isEqualTo(3L);
        assertThat(bh.getHours()).isEqualByComparingTo(new BigDecimal("4.25"));
        assertThat(bh.getNote()).isEqualTo("Support");
        assertThat(bh.getDateLogged()).isEqualTo(dateLogged);
        assertThat(bh.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void settersUpdateAllFields() {
        BillableHour bh = new BillableHour();
        LocalDate date = new LocalDate(2024, 7, 1);
        DateTime now = DateTime.now();

        bh.setId(99L);
        bh.setCustomerId(10L);
        bh.setUserId(20L);
        bh.setCategoryId(30L);
        bh.setHours(new BigDecimal("6.00"));
        bh.setNote("Updated note");
        bh.setDateLogged(date);
        bh.setCreatedAt(now);

        assertThat(bh.getId()).isEqualTo(99L);
        assertThat(bh.getCustomerId()).isEqualTo(10L);
        assertThat(bh.getUserId()).isEqualTo(20L);
        assertThat(bh.getCategoryId()).isEqualTo(30L);
        assertThat(bh.getHours()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(bh.getNote()).isEqualTo("Updated note");
        assertThat(bh.getDateLogged()).isEqualTo(date);
        assertThat(bh.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void storesForeignKeyIdsNotEntities() {
        // Characterisation: BillableHour stores FK IDs (Long), not entity references
        BillableHour bh = new BillableHour(1L, 2L, 3L, new BigDecimal("1.00"), "note", LocalDate.now());
        assertThat(bh.getCustomerId()).isInstanceOf(Long.class);
        assertThat(bh.getUserId()).isInstanceOf(Long.class);
        assertThat(bh.getCategoryId()).isInstanceOf(Long.class);
    }

    @Test
    void dateLoggedUsesJodaLocalDate() {
        // Characterisation: dateLogged is Joda LocalDate, not java.time.LocalDate
        BillableHour bh = new BillableHour(1L, 2L, 3L, new BigDecimal("1.00"), "note", LocalDate.now());
        assertThat(bh.getDateLogged()).isInstanceOf(LocalDate.class);
    }

    @Test
    void createdAtUsesJodaDateTime() {
        // Characterisation: createdAt is Joda DateTime
        BillableHour bh = new BillableHour(1L, 2L, 3L, new BigDecimal("1.00"), "note", LocalDate.now());
        assertThat(bh.getCreatedAt()).isInstanceOf(DateTime.class);
    }
}
