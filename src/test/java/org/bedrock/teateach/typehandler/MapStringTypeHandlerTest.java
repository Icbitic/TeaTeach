package org.bedrock.teateach.typehandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapStringTypeHandlerTest {

    private MapStringTypeHandler typeHandler;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeHandler = new MapStringTypeHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        // Create a sample map
        Map<String, Double> map = new HashMap<>();
        map.put("math", 95.5);
        map.put("physics", 85.0);

        // Call the method
        typeHandler.setNonNullParameter(ps, 1, map, null);

        // Verify the PreparedStatement was called with the right JSON string
        verify(ps).setString(eq(1), contains("\"math\":"));
        verify(ps).setString(eq(1), contains("\"physics\":"));
        verify(ps).setString(eq(1), contains("95.5"));
        verify(ps).setString(eq(1), contains("85.0"));
    }

    @Test
    void testGetNullableResultByColumnName() throws SQLException {
        // Setup mock to return a JSON string
        String json = "{\"math\":95.5, \"physics\":85.0}";
        when(rs.getString("map_column")).thenReturn(json);

        // Call the method
        Map<String, Double> result = typeHandler.getNullableResult(rs, "map_column");

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(95.5, result.get("math"));
        assertEquals(85.0, result.get("physics"));
    }

    @Test
    void testGetNullableResultByColumnIndex() throws SQLException {
        // Setup mock to return a JSON string
        String json = "{\"math\":95.5, \"physics\":85.0}";
        when(rs.getString(1)).thenReturn(json);

        // Call the method
        Map<String, Double> result = typeHandler.getNullableResult(rs, 1);

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(95.5, result.get("math"));
        assertEquals(85.0, result.get("physics"));
    }

    @Test
    void testGetNullableResultFromCallableStatement() throws SQLException {
        // Setup mock to return a JSON string
        String json = "{\"math\":95.5, \"physics\":85.0}";
        when(cs.getString(1)).thenReturn(json);

        // Call the method
        Map<String, Double> result = typeHandler.getNullableResult(cs, 1);

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(95.5, result.get("math"));
        assertEquals(85.0, result.get("physics"));
    }

    @Test
    void testHandleNullResult() throws SQLException {
        // Setup mock to return null
        when(rs.getString("map_column")).thenReturn(null);

        // Call the method
        Map<String, Double> result = typeHandler.getNullableResult(rs, "map_column");

        // Verify the result is an empty map
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleEmptyJsonResult() throws SQLException {
        // Setup mock to return empty JSON
        when(rs.getString("map_column")).thenReturn("{}");

        // Call the method
        Map<String, Double> result = typeHandler.getNullableResult(rs, "map_column");

        // Verify the result is an empty map
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleEmptyStringResult() throws SQLException {
        // Setup mock to return empty string
        when(rs.getString("map_column")).thenReturn("");

        // Call the method
        Map<String, Double> result = typeHandler.getNullableResult(rs, "map_column");

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
