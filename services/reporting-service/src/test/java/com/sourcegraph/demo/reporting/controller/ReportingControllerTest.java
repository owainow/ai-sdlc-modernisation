package com.sourcegraph.demo.reporting.controller;

import com.sourcegraph.demo.reporting.service.ReportingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ReportingController.class)
@ActiveProfiles("test")
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportingService reportingService;

    @Test
    void getMonthlyReport_returnsData() throws Exception {
        Map<String, Object> report = Map.of(
                "year", 2026,
                "month", 2,
                "grandTotalHours", new BigDecimal("160.0"),
                "grandTotalAmount", new BigDecimal("24000.00"),
                "customers", List.of()
        );
        when(reportingService.getMonthlyReport(eq(2026), eq(2), any())).thenReturn(report);

        mockMvc.perform(get("/api/v1/reports/monthly")
                        .param("year", "2026")
                        .param("month", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.year", is(2026)))
                .andExpect(jsonPath("$.data.month", is(2)));
    }

    @Test
    void getRangeReport_returnsData() throws Exception {
        Map<String, Object> report = Map.of(
                "fromDate", "2026-01-01",
                "toDate", "2026-02-23",
                "grandTotalHours", new BigDecimal("320.0"),
                "grandTotalAmount", new BigDecimal("48000.00"),
                "customers", List.of()
        );
        when(reportingService.getRangeReport(any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(report);

        mockMvc.perform(get("/api/v1/reports/range")
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-02-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));
    }

    @Test
    void getUtilisationReport_returnsData() throws Exception {
        UUID userId = UUID.randomUUID();
        Map<String, Object> report = Map.of(
                "userId", userId,
                "year", 2026,
                "month", 2,
                "totalHours", new BigDecimal("160.0"),
                "customers", List.of()
        );
        when(reportingService.getUtilisationReport(eq(2026), eq(2), eq(userId))).thenReturn(report);

        mockMvc.perform(get("/api/v1/reports/utilisation")
                        .param("year", "2026")
                        .param("month", "2")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));
    }
}
