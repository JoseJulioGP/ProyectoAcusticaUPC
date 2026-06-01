package com.upc.acusticupc.reports.infrastructure.web;

import com.upc.acusticupc.reports.application.service.CompliancePdfReportService;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ReportControllerIntegrationTest {

    private static final String FROM = "2026-01-01";
    private static final String TO   = "2026-01-31";

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private CompliancePdfReportService pdfService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private byte[] pdfBytes() {
        return new byte[]{'%', 'P', 'D', 'F', '-'};
    }

    private void assertPdfHeader(byte[] body) {
        assertEquals('%', (char) body[0]);
        assertEquals('P', (char) body[1]);
        assertEquals('D', (char) body[2]);
        assertEquals('F', (char) body[3]);
    }

    @Test
    void pdf_sinToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/compliance/pdf")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void pdf_conViewer_returns200() throws Exception {
        when(pdfService.generate(any(), any(), any())).thenReturn(pdfBytes());

        mockMvc.perform(get("/api/v1/reports/compliance/pdf")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/pdf")))
                .andExpect(result -> assertPdfHeader(result.getResponse().getContentAsByteArray()));
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void pdf_conAnalyst_returns200_yContentTypePdf() throws Exception {
        when(pdfService.generate(any(), any(), any())).thenReturn(pdfBytes());

        mockMvc.perform(get("/api/v1/reports/compliance/pdf")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/pdf")))
                .andExpect(result -> assertPdfHeader(result.getResponse().getContentAsByteArray()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pdf_conAdmin_returns200_yContentTypePdf() throws Exception {
        when(pdfService.generate(any(), any(), any())).thenReturn(pdfBytes());

        mockMvc.perform(get("/api/v1/reports/compliance/pdf")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/pdf")))
                .andExpect(result -> assertPdfHeader(result.getResponse().getContentAsByteArray()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pdf_sinFromYTo_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/reports/compliance/pdf"))
                .andExpect(status().isBadRequest());
    }
}
