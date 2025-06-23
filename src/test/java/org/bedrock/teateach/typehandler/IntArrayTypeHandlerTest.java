package org.bedrock.teateach.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class IntArrayTypeHandlerTest {

    private IntArrayTypeHandler typeHandler;

    @Mock
    private PreparedStatement ps;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    @Mock
    private Array sqlArray;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeHandler = new IntArrayTypeHandler();
    }

    @Test
    void shouldUsePostgresArrayForPostgreSQL() throws SQLException {
        // Given
        int[] parameter = {1, 2, 3};
        when(ps.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(connection.createArrayOf(eq("integer"), any())).thenReturn(sqlArray);

        // When
        typeHandler.setNonNullParameter(ps, 1, parameter, JdbcType.ARRAY);

        // Then
        verify(connection).createArrayOf(eq("integer"), any());
        verify(ps).setArray(1, sqlArray);
    }

    @Test
    void shouldUseStringForNonPostgreSQLDatabases() throws SQLException {
        // Given
        int[] parameter = {10, 20, 30};
        when(ps.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");

        // When
        typeHandler.setNonNullParameter(ps, 1, parameter, JdbcType.VARCHAR);

        // Then
        verify(ps).setString(1, "10,20,30");
    }

    @Test
    void shouldFallbackToStringOnException() throws SQLException {
        // Given
        int[] parameter = {100, 200, 300};
        when(ps.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(connection.createArrayOf(anyString(), any())).thenThrow(new SQLException("Test exception"));

        // When
        typeHandler.setNonNullParameter(ps, 1, parameter, JdbcType.VARCHAR);

        // Then
        verify(ps).setString(1, "100,200,300");
    }

    @Test
    void shouldParseArrayTypeFromResultSet() throws SQLException {
        // Given
        Integer[] javaArray = {1, 2, 3};
        when(rs.getObject("col")).thenReturn(sqlArray);
        when(sqlArray.getArray()).thenReturn(javaArray);

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test
    void shouldParseStringArrayFromResultSet() throws SQLException {
        // Given
        when(rs.getObject("col")).thenReturn("10,20,30");

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[]{10, 20, 30}, result);
    }

    @Test
    void shouldHandleEmptyStringFromResultSet() throws SQLException {
        // Given
        when(rs.getObject("col")).thenReturn("");

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[0], result);
    }

    @Test
    void shouldHandleNullFromResultSet() throws SQLException {
        // Given
        when(rs.getObject("col")).thenReturn(null);

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[0], result);
    }

    @Test
    void shouldHandleInvalidNumbersInString() throws SQLException {
        // Given
        when(rs.getObject("col")).thenReturn("10,invalid,30");

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[]{10, 0, 30}, result);
    }

    @Test
    void shouldGetNullableResultByColumnIndex() throws SQLException {
        // Given
        when(rs.getObject(1)).thenReturn("100,200,300");

        // When
        int[] result = typeHandler.getNullableResult(rs, 1);

        // Then
        assertArrayEquals(new int[]{100, 200, 300}, result);
    }

    @Test
    void shouldGetNullableResultFromCallableStatement() throws SQLException {
        // Given
        when(cs.getObject(1)).thenReturn("1000,2000,3000");

        // When
        int[] result = typeHandler.getNullableResult(cs, 1);

        // Then
        assertArrayEquals(new int[]{1000, 2000, 3000}, result);
    }

    @Test
    void shouldHandleNativeIntArrayFromPostgres() throws SQLException {
        // Given
        int[] nativeArray = {1, 2, 3};
        when(rs.getObject("col")).thenReturn(sqlArray);
        when(sqlArray.getArray()).thenReturn(nativeArray);

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test
    void shouldHandleSqlExceptionWhenGettingArray() throws SQLException {
        // Given
        when(rs.getObject("col")).thenReturn(sqlArray);
        when(sqlArray.getArray()).thenThrow(new SQLException("Test error"));

        // When
        int[] result = typeHandler.getNullableResult(rs, "col");

        // Then
        assertArrayEquals(new int[0], result);
    }
}
