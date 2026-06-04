package com.upc.acusticupc.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para ITs que requieren Postgres real (SQL nativo Postgres-only:
 * EXTRACT(DOW), date_trunc, CAST AS uuid, columnas generadas).
 *
 * Levanta postgres:16-alpine con Testcontainers, aplica las migraciones
 * Flyway V1–V8 al arrancar y valida el esquema contra las entidades.
 *
 * Requiere Docker corriendo en local. Los ITs que NO necesitan Postgres
 * siguen usando H2 (src/test/resources/application.yml), más rápidos.
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractPostgresIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        // Sobre Postgres real sí corremos Flyway (V1–V8) y validamos el esquema.
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
