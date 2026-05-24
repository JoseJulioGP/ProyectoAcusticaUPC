package com.upc.acusticupc.ingestion.infrastructure.parser;

import com.upc.acusticupc.ingestion.application.dto.ParseResult;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class TsvSonometerParserTest {

    private final TsvSonometerParser parser = new TsvSonometerParser();

    @Test
    void parsesSampleFile() throws Exception {
        try (InputStream in = new ClassPathResource("sonometer-samples/format_a.xls").getInputStream()) {
            ParseResult result = parser.parse(in);
            assertTrue(result.measurements().size() > 0);
            assertEquals("dBA", result.measurements().get(0).unit());
            assertTrue(result.measurements().get(0).dbValue() > 0);
        }
    }
}
