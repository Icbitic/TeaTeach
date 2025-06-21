package org.bedrock.teateach.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * TypeHandler for mapping Map<String, Double> to/from JSON string in database.
 */
@MappedJdbcTypes(JdbcType.VARCHAR) // Maps to VARCHAR/TEXT in DB
@MappedTypes(Map.class) // Maps Java Map
public class MapStringTypeHandler extends BaseTypeHandler<Map<String, Double>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Double> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting Map<String, Double> to JSON string", e);
        }
    }

    @Override
    public Map<String, Double> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJsonToMap(json);
    }

    @Override
    public Map<String, Double> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJsonToMap(json);
    }

    @Override
    public Map<String, Double> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJsonToMap(json);
    }

    private Map<String, Double> parseJsonToMap(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting JSON string to Map<String, Double>", e);
        }
    }
}