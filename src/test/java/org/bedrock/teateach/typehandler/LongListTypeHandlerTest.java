package org.bedrock.teateach.typehandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LongListTypeHandlerTest {

    private LongListTypeHandler typeHandler;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeHandler = new LongListTypeHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        // Create a sample list
        List<Long> list = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        // Call the method
        typeHandler.setNonNullParameter(ps, 1, list, null);

        // Verify the PreparedStatement was called with the right JSON string
        verify(ps).setString(eq(1), eq("[1,2,3,4,5]"));
    }

    @Test
    void testGetNullableResultByColumnName() throws SQLException {
        // Setup mock to return a JSON string
        String json = "[1,2,3,4,5]";
        when(rs.getString("list_column")).thenReturn(json);

        // Call the method
        List<Long> result = typeHandler.getNullableResult(rs, "list_column");

        // Verify the result
        assertEquals(5, result.size());
        assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), result);
    }

    @Test
    void testGetNullableResultByColumnIndex() throws SQLException {
        // Setup mock to return a JSON string
        String json = "[1,2,3,4,5]";
        when(rs.getString(1)).thenReturn(json);

        // Call the method
        List<Long> result = typeHandler.getNullableResult(rs, 1);

        // Verify the result
        assertEquals(5, result.size());
        assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), result);
    }

    @Test
    void testGetNullableResultFromCallableStatement() throws SQLException {
        // Setup mock to return a JSON string
        String json = "[1,2,3,4,5]";
        when(cs.getString(1)).thenReturn(json);

        // Call the method
        List<Long> result = typeHandler.getNullableResult(cs, 1);

        // Verify the result
        assertEquals(5, result.size());
        assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), result);
    }

    @Test
    void testHandleNullResult() throws SQLException {
        // Setup mock to return null
        when(rs.getString("list_column")).thenReturn(null);

        // Call the method
        List<Long> result = typeHandler.getNullableResult(rs, "list_column");

        // Verify the result is an empty list
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleEmptyJsonResult() throws SQLException {
        // Setup mock to return empty array
        when(rs.getString("list_column")).thenReturn("[]");

        // Call the method
        List<Long> result = typeHandler.getNullableResult(rs, "list_column");

        // Verify the result is an empty list
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleEmptyStringResult() throws SQLException {
        // Setup mock to return empty string
        when(rs.getString("list_column")).thenReturn("");

        // Call the method
        List<Long> result = typeHandler.getNullableResult(rs, "list_column");

        // Verify the result is an empty list
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleInvalidJsonResult() {
        // Setup mock to return invalid JSON
        try {
            when(rs.getString("list_column")).thenReturn("invalid-json");

            // Call the method and expect SQLException
            assertThrows(SQLException.class, () -> {
                typeHandler.getNullableResult(rs, "list_column");
            });
        } catch (SQLException e) {
            fail("SQLException should not be thrown during test setup");
        }
    }
}
