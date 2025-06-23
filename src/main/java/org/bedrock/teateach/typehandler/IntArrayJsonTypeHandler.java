package org.bedrock.teateach.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

/**
 * Custom TypeHandler for storing and retrieving int[] arrays to/from MySQL using JSON format.
 * MySQL doesn't support native array types, so this handler serializes the array to JSON.
 */
@MappedTypes(int[].class)
public class IntArrayJsonTypeHandler extends BaseTypeHandler<int[]> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, int[] parameter, JdbcType jdbcType) throws SQLException {
        try {
            // Convert int[] to JSON string, ensuring null values become empty arrays
            int[] safeParam = parameter != null ? parameter : new int[0];
            String jsonArray = objectMapper.writeValueAsString(safeParam);
            ps.setString(i, jsonArray);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting int[] to JSON", e);
        }
    }

    @Override
    public int[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJsonArray(rs.getString(columnName));
    }

    @Override
    public int[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJsonArray(rs.getString(columnIndex));
    }

    @Override
    public int[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJsonArray(cs.getString(columnIndex));
    }

    private int[] parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return new int[0];
        }

        try {
            // Parse JSON string to int[]
            return objectMapper.readValue(json, int[].class);
        } catch (JsonProcessingException e) {
            // Return empty array if parsing fails
            return new int[0];
        }
    }
}
