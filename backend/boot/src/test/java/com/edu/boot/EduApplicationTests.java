package com.edu.boot;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EduApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Application context loads successfully")
    void applicationContextLoads() {
        assertThat(restTemplate).isNotNull();
        assertThat(dataSource).isNotNull();
        assertThat(flyway).isNotNull();
    }

    @Test
    @DisplayName("Actuator health endpoint returns UP with database component")
    void healthEndpointReturnsUpWithDatabase() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
        assertThat(response.getBody()).contains("\"db\"");
    }

    @Test
    @DisplayName("PostgreSQL DataSource establishes a valid connection")
    void dataSourceConnectsToPostgreSQL() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(5)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
        }
    }

    @Test
    @DisplayName("Flyway executes migrations through V4 and creates migration history")
    void flywayMigrationAppliedSuccessfully() {
        MigrationInfo current = flyway.info().current();
        assertThat(current).isNotNull();
        assertThat(current.getVersion().compareTo(org.flywaydb.core.api.MigrationVersion.fromVersion("4"))).isGreaterThanOrEqualTo(0);
        assertThat(current.getState().isApplied()).isTrue();

        // Verify Phase 2.5 tables exist
        List<String> identityTables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'identity'",
                String.class
        );
        assertThat(identityTables).contains("users");

        List<String> organizationTables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'organization'",
                String.class
        );
        assertThat(organizationTables).contains("organizations", "organization_memberships");
    }

    @Test
    @DisplayName("All 12 Modular Monolith Schemas are initialized in PostgreSQL")
    void allModularMonolithSchemasExist() {
        List<String> expectedSchemas = List.of(
                "identity", "organization", "configuration", "people",
                "curriculum", "academic", "enrollment", "teaching",
                "attendance", "assessment", "certification", "rule_engine"
        );

        List<String> actualSchemas = jdbcTemplate.queryForList(
                "SELECT nspname FROM pg_namespace WHERE nspname NOT LIKE 'pg_%' AND nspname != 'information_schema'",
                String.class
        );

        assertThat(actualSchemas).containsAll(expectedSchemas);
    }
}
