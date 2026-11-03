package com.edu.boot.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom {@link HealthIndicator} kiểm tra kết nối và trạng thái của các PostgreSQL schemas
 * cốt lõi trong kiến trúc Modular Monolith Multi-Schema: {@code identity} và {@code organization}.
 * <p>
 * Hiển thị tại endpoint {@code /actuator/health} dưới thành phần {@code multiSchema}.
 */
@Component("multiSchema")
public class MultiSchemaHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(MultiSchemaHealthIndicator.class);

    private static final List<String> REQUIRED_SCHEMAS = List.of("identity", "organization");
    private static final int QUERY_TIMEOUT_SECONDS = 5;

    private final DataSource dataSource;

    public MultiSchemaHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            boolean allHealthy = true;

            for (String schema : REQUIRED_SCHEMAS) {
                try {
                    int tableCount = checkSchemaTables(conn, schema);
                    details.put("schema." + schema, "UP (tables: " + tableCount + ")");
                } catch (SQLException e) {
                    allHealthy = false;
                    log.warn("Health check failed for schema '{}': {}", schema, e.getMessage());
                    details.put("schema." + schema, "DOWN (" + e.getMessage() + ")");
                }
            }

            if (allHealthy) {
                return Health.up().withDetails(details).build();
            } else {
                return Health.down().withDetails(details).build();
            }

        } catch (Exception e) {
            log.error("Failed to obtain database connection for multiSchema health check", e);
            return Health.down(e)
                    .withDetail("error", "Database connection failed: " + e.getMessage())
                    .withDetails(details)
                    .build();
        }
    }

    private int checkSchemaTables(Connection conn, String schemaName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            stmt.setString(1, schemaName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}

