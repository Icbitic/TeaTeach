package org.bedrock.teateach.typehandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalDateTypeHandlerTest {

    private LocalDateTypeHandler typeHandler;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeHandler = new LocalDateTypeHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        // Create a sample LocalDate
        LocalDate date = LocalDate.of(2025, 6, 23);

        // Call the method
        typeHandler.setNonNullParameter(ps, 1, date, null);

        // Verify the PreparedStatement was called with the right Date
        verify(ps).setDate(eq(1), eq(Date.valueOf(date)));
    }

    @Test
    void testGetNullableResultByColumnName() throws SQLException {
        // Setup mock to return a Date
        LocalDate expectedDate = LocalDate.of(2025, 6, 23);
        when(rs.getDate("date_column")).thenReturn(Date.valueOf(expectedDate));

        // Call the method
        LocalDate result = typeHandler.getNullableResult(rs, "date_column");

        // Verify the result
        assertEquals(expectedDate, result);
    }

    @Test
    void testGetNullableResultByColumnIndex() throws SQLException {
        // Setup mock to return a Date
        LocalDate expectedDate = LocalDate.of(2025, 6, 23);
        when(rs.getDate(1)).thenReturn(Date.valueOf(expectedDate));

        // Call the method
        LocalDate result = typeHandler.getNullableResult(rs, 1);

        // Verify the result
        assertEquals(expectedDate, result);
    }

    @Test
    void testGetNullableResultFromCallableStatement() throws SQLException {
        // Setup mock to return a Date
        LocalDate expectedDate = LocalDate.of(2025, 6, 23);
        when(cs.getDate(1)).thenReturn(Date.valueOf(expectedDate));

        // Call the method
        LocalDate result = typeHandler.getNullableResult(cs, 1);

        // Verify the result
        assertEquals(expectedDate, result);
    }

    @Test
    void testHandleNullResult() throws SQLException {
        // Setup mock to return null
        when(rs.getDate("date_column")).thenReturn(null);

        // Call the method
        LocalDate result = typeHandler.getNullableResult(rs, "date_column");

        // Verify the result is null
        assertNull(result);
    }
}
