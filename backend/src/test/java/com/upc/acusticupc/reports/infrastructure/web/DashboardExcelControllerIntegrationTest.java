package com.upc.acusticupc.reports.infrastructure.web;

import com.upc.acusticupc.reports.application.service.CompliancePdfReportService;
import com.upc.acusticupc.reports.application.service.DashboardExcelService;
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

@SpringBootTest
class DashboardExcelControllerIntegrationTest {

    private static final String FROM = "2026-01-01";
    private static final String TO   = "2026-01-31";
    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private CompliancePdfReportService pdfService;

    @MockitoBean
    private DashboardExcelService excelService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private byte[] xlsxBytes() {
        // Magic bytes de un ZIP/OOXML (PK\x03\x04). Contenido no se valida.
        return new byte[]{'P', 'K', 0x03, 0x04};
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void excel_conViewer_retorna200_conContentTypeYDisposition() throws Exception {
        when(excelService.generate(any(), any(), any())).thenReturn(xlsxBytes());

        mockMvc.perform(get("/api/v1/reports/dashboard.xlsx")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString(XLSX_MIME)))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("dashboard.xlsx")));
    }

    @Test
    void excel_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/dashboard.xlsx")
                        .param("from", FROM).param("to", TO))
                .andExpect(status().isUnauthorized());
    }
}
