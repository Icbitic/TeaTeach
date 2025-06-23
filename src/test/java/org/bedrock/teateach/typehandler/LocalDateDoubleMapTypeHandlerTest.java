package org.bedrock.teateach.typehandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalDateDoubleMapTypeHandlerTest {

    private LocalDateDoubleMapTypeHandler typeHandler;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeHandler = new LocalDateDoubleMapTypeHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        // Create a sample map
        Map<LocalDate, Double> map = new HashMap<>();
        map.put(LocalDate.of(2025, 6, 23), 95.5);
        map.put(LocalDate.of(2025, 6, 24), 85.0);

        // Call the method
        typeHandler.setNonNullParameter(ps, 1, map, null);

        // Verify the PreparedStatement was called with the right JSON string
        verify(ps).setString(eq(1), contains("\"2025-06-23\":"));
        verify(ps).setString(eq(1), contains("\"2025-06-24\":"));
        verify(ps).setString(eq(1), contains("95.5"));
        verify(ps).setString(eq(1), contains("85.0"));
    }

    @Test
    void testGetNullableResultByColumnName() throws SQLException {
        // Setup mock to return a JSON string with proper format
        String json = "{\"2025-06-23\":95.5,\"2025-06-24\":85.0}";
        when(rs.getString("map_column")).thenReturn(json);

        // Call the method
        Map<LocalDate, Double> result = typeHandler.getNullableResult(rs, "map_column");

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(95.5, result.get(LocalDate.of(2025, 6, 23)));
        assertEquals(85.0, result.get(LocalDate.of(2025, 6, 24)));
    }

    @Test
    void testGetNullableResultByColumnIndex() throws SQLException {
        // Setup mock to return a JSON string with proper format
        String json = "{\"2025-06-23\":95.5,\"2025-06-24\":85.0}";
        when(rs.getString(1)).thenReturn(json);

        // Call the method
        Map<LocalDate, Double> result = typeHandler.getNullableResult(rs, 1);

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(95.5, result.get(LocalDate.of(2025, 6, 23)));
        assertEquals(85.0, result.get(LocalDate.of(2025, 6, 24)));
    }

    @Test
    void testGetNullableResultFromCallableStatement() throws SQLException {
        // Setup mock to return a JSON string with proper format
        String json = "{\"2025-06-23\":95.5,\"2025-06-24\":85.0}";
        when(cs.getString(1)).thenReturn(json);

        // Call the method
        Map<LocalDate, Double> result = typeHandler.getNullableResult(cs, 1);

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(95.5, result.get(LocalDate.of(2025, 6, 23)));
        assertEquals(85.0, result.get(LocalDate.of(2025, 6, 24)));
    }

    @Test
    void testHandleNullResult() throws SQLException {
        // Setup mock to return null
        when(rs.getString("map_column")).thenReturn(null);

        // Call the method
        Map<LocalDate, Double> result = typeHandler.getNullableResult(rs, "map_column");

        // Verify the result is an empty map
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleEmptyJsonResult() throws SQLException {
        // Setup mock to return empty JSON
        when(rs.getString("map_column")).thenReturn("{}");

        // Call the method
        Map<LocalDate, Double> result = typeHandler.getNullableResult(rs, "map_column");

        // Verify the result is an empty map
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleInvalidJsonResult() {
        // Setup mock to return invalid JSON
        try {
            when(rs.getString("map_column")).thenReturn("invalid-json");

            // Call the method and expect SQLException
            assertThrows(SQLException.class, () -> {
                typeHandler.getNullableResult(rs, "map_column");
            });
        } catch (SQLException e) {
            fail("SQLException should not be thrown during test setup");
        }
    }
}
