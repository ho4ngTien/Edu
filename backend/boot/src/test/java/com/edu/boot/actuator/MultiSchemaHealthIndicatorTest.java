package com.edu.boot.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiSchemaHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private MultiSchemaHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new MultiSchemaHealthIndicator(dataSource);
    }

    @Test
    @DisplayName("Should return UP when all required schemas exist and return table counts")
    void shouldReturnUpWhenAllSchemasAreHealthy() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true);
        when(resultSet.getInt(1)).thenReturn(2, 3); // 2 tables in identity, 3 in organization

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("schema.identity", "UP (tables: 2)");
        assertThat(health.getDetails()).containsEntry("schema.organization", "UP (tables: 3)");
    }

    @Test
    @DisplayName("Should return DOWN when database connection cannot be established")
    void shouldReturnDownWhenConnectionFails() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
        assertThat((String) health.getDetails().get("error")).contains("Connection refused");
    }

    @Test
    @DisplayName("Should return DOWN when a required schema query fails with SQLException")
    void shouldReturnDownWhenSchemaQueryFails() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Schema not accessible"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("schema.identity").toString()).contains("DOWN");
    }
}

