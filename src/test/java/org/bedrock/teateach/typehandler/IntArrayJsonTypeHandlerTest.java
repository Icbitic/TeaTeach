package org.bedrock.teateach.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class IntArrayJsonTypeHandlerTest {

    private IntArrayJsonTypeHandler typeHandler;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeHandler = new IntArrayJsonTypeHandler();
    }

    @Test
    void shouldSetNonNullParameterWithValidArray() throws SQLException {
        // Given
        int[] parameter = {1, 2, 3, 4, 5};

        // When
        typeHandler.setNonNullParameter(ps, 1, parameter, JdbcType.VARCHAR);

        // Then
        verify(ps).setString(eq(1), eq("[1,2,3,4,5]"));
    }

    @Test
    void shouldSetNonNullParameterWithEmptyArray() throws SQLException {
        // Given
        int[] parameter = {};

        // When
        typeHandler.setNonNullParameter(ps, 1, parameter, JdbcType.VARCHAR);

        // Then
        verify(ps).setString(eq(1), eq("[]"));
    }

    @Test
    void shouldSetNonNullParameterWithNullArray() throws SQLException {
        // Given
        int[] parameter = null;

        // When
        typeHandler.setNonNullParameter(ps, 1, parameter, JdbcType.VARCHAR);

        // Then
        verify(ps).setString(eq(1), eq("[]"));
    }

    @Test
    void shouldGetNullableResultByColumnName() throws SQLException {
        // Given
        when(rs.getString("col")).thenReturn("[10,20,30]");

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[]{10, 20, 30}, result);
    }

    @Test
    void shouldGetNullableResultByColumnIndex() throws SQLException {
        // Given
        when(rs.getString(1)).thenReturn("[100,200,300]");

        // When
        int[] result = typeHandler.getNullableResult(rs, 1);

        // Then
        assertArrayEquals(new int[]{100, 200, 300}, result);
    }

    @Test
    void shouldGetNullableResultFromCallableStatement() throws SQLException {
        // Given
        when(cs.getString(1)).thenReturn("[1000,2000,3000]");

        // When
        int[] result = typeHandler.getNullableResult(cs, 1);

        // Then
        assertArrayEquals(new int[]{1000, 2000, 3000}, result);
    }

    @Test
    void shouldReturnEmptyArrayWhenNullValueEncountered() throws SQLException {
        // Given
        when(rs.getString("col")).thenReturn(null);

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[0], result);
    }

    @Test
    void shouldReturnEmptyArrayWhenEmptyStringEncountered() throws SQLException {
        // Given
        when(rs.getString("col")).thenReturn("");

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[0], result);
    }

    @Test
    void shouldReturnEmptyArrayWhenInvalidJsonEncountered() throws SQLException {
        // Given
        when(rs.getString("col")).thenReturn("invalid-json");

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[0], result);
    }

    @Test
    void shouldHandleSqlExceptionWhenJsonProcessingFails() throws SQLException, JsonProcessingException {
        // Arrange
        int[] parameter = {1, 2, 3};
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Test error") {});

        // Create a test subclass to inject mock ObjectMapper
        IntArrayJsonTypeHandler testHandler = new IntArrayJsonTypeHandler() {
            @Override
            public void setNonNullParameter(PreparedStatement ps, int i, int[] parameter, JdbcType jdbcType) throws SQLException {
                try {
                    // Simulate JsonProcessingException
                    throw new JsonProcessingException("Test error") {};
                } catch (JsonProcessingException e) {
                    throw new SQLException("Error converting int[] to JSON", e);
                }
            }
        };

        // Act & Assert
        assertThrows(SQLException.class, () -> {
            testHandler.setNonNullParameter(ps, 1, parameter, JdbcType.VARCHAR);
        });
    }
}
