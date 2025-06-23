package org.bedrock.teateach.typehandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalDateTimeTypeHandlerTest {

    private LocalDateTimeTypeHandler typeHandler;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeHandler = new LocalDateTimeTypeHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        // Create a sample LocalDateTime
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 23, 14, 30, 15);

        // Call the method
        typeHandler.setNonNullParameter(ps, 1, dateTime, null);

        // Verify the PreparedStatement was called with the right Timestamp
        verify(ps).setTimestamp(eq(1), eq(Timestamp.valueOf(dateTime)));
    }

    @Test
    void testGetNullableResultByColumnName() throws SQLException {
        // Setup mock to return a Timestamp
        LocalDateTime expectedDateTime = LocalDateTime.of(2025, 6, 23, 14, 30, 15);
        when(rs.getTimestamp("datetime_column")).thenReturn(Timestamp.valueOf(expectedDateTime));

        // Call the method
        LocalDateTime result = typeHandler.getNullableResult(rs, "datetime_column");

        // Verify the result
        assertEquals(expectedDateTime, result);
    }

    @Test
    void testGetNullableResultByColumnIndex() throws SQLException {
        // Setup mock to return a Timestamp
        LocalDateTime expectedDateTime = LocalDateTime.of(2025, 6, 23, 14, 30, 15);
        when(rs.getTimestamp(1)).thenReturn(Timestamp.valueOf(expectedDateTime));

        // Call the method
        LocalDateTime result = typeHandler.getNullableResult(rs, 1);

        // Verify the result
        assertEquals(expectedDateTime, result);
    }

    @Test
    void testGetNullableResultFromCallableStatement() throws SQLException {
        // Setup mock to return a Timestamp
        LocalDateTime expectedDateTime = LocalDateTime.of(2025, 6, 23, 14, 30, 15);
        when(cs.getTimestamp(1)).thenReturn(Timestamp.valueOf(expectedDateTime));

        // Call the method
        LocalDateTime result = typeHandler.getNullableResult(cs, 1);

        // Verify the result
        assertEquals(expectedDateTime, result);
    }

    @Test
    void testHandleNullResult() throws SQLException {
        // Setup mock to return null
        when(rs.getTimestamp("datetime_column")).thenReturn(null);

        // Call the method
        LocalDateTime result = typeHandler.getNullableResult(rs, "datetime_column");

        // Verify the result is null
        assertNull(result);
    }
}
