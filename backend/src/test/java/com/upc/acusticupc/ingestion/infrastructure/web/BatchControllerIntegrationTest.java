package com.upc.acusticupc.ingestion.infrastructure.web;

import com.upc.acusticupc.ingestion.application.service.BatchQueryService;
import com.upc.acusticupc.ingestion.application.service.SonometerIngestionService;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BatchControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SonometerIngestionService ingestionService;

    @MockitoBean
    private BatchQueryService queryService;

    private MockMvc mockMvc;

    @PostConstruct
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadReturns202WhenAdmin() throws Exception {
        UUID batchId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        when(ingestionService.registerBatch(any(), eq(zoneId), any()))
                .thenReturn(batchId);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ingest/batches")
                        .file(file)
                        .param("zoneId", zoneId.toString()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.batchId").value(batchId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void uploadIsForbiddenForNonAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "fake".getBytes()
        );
        mockMvc.perform(multipart("/api/v1/ingest/batches")
                        .file(file)
                        .param("zoneId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }
}