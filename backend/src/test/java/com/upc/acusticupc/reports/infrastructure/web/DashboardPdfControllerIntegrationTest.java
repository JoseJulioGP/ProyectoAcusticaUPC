package com.upc.acusticupc.reports.infrastructure.web;

import com.upc.acusticupc.reports.application.service.CompliancePdfReportService;
import com.upc.acusticupc.reports.application.service.DashboardExcelService;
import com.upc.acusticupc.reports.application.service.DashboardPdfService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 8 · IT del endpoint {@code GET /api/v1/reports/dashboard.pdf}.
 */
@SpringBootTest
class DashboardPdfControllerIntegrationTest {

    private static final String FROM = "2026-01-01";
    private static final String TO   = "2026-01-31";

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private CompliancePdfReportService pdfService;

    @MockitoBean
    private DashboardExcelService excelService;

    @MockitoBean
    private DashboardPdfService dashboardPdfService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private byte[] pdfBytes() {
        // Magic bytes %PDF-... el controller no valida el contenido.
        return new byte[]{'%', 'P', 'D', 'F', '-'};
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void pdfDashboard_conViewer_retorna200_yContentTypePdf() throws Exception {
        when(dashboardPdfService.generate(any(), any(), any())).thenReturn(pdfBytes());

        mockMvc.perform(get("/api/v1/reports/dashboard.pdf")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                        containsString("application/pdf")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("dashboard.pdf")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pdfDashboard_conAdmin_retorna200() throws Exception {
        when(dashboardPdfService.generate(any(), any(), any())).thenReturn(pdfBytes());

        mockMvc.perform(get("/api/v1/reports/dashboard.pdf")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isOk());
    }

    @Test
    void pdfDashboard_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/dashboard.pdf")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pdfDashboard_sinParams_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/reports/dashboard.pdf"))
                .andExpect(status().isBadRequest());
    }
}
