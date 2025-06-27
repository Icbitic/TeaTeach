package org.bedrock.teateach.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom TypeHandler for storing and retrieving List<Long> to/from MySQL using JSON format.
 * MySQL doesn't support native array types, so this handler serializes the list to JSON.
 */
@MappedTypes(List.class)
public class LongListJsonTypeHandler extends BaseTypeHandler<List<Long>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<Long>> typeReference = new TypeReference<List<Long>>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        try {
            // Convert List<Long> to JSON string, ensuring null values become empty arrays
            List<Long> safeParam = parameter != null ? parameter : new ArrayList<>();
            String jsonArray = objectMapper.writeValueAsString(safeParam);
            ps.setString(i, jsonArray);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting List<Long> to JSON", e);
        }
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJsonArray(rs.getString(columnName));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJsonArray(rs.getString(columnIndex));
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJsonArray(cs.getString(columnIndex));
    }

    private List<Long> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            // If parsing fails, return empty list instead of throwing exception
            return new ArrayList<>();
        }
    }
}