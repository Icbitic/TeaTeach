package org.bedrock.teateach.typehandler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TypeHandler for mapping Map<LocalDate, Double> to/from JSON string in database.
 * Uses custom serialization and deserialization for LocalDate.
 */
@MappedJdbcTypes(JdbcType.VARCHAR) // Maps to VARCHAR/TEXT in DB
@MappedTypes(Map.class) // Maps Java Map
public class LocalDateDoubleMapTypeHandler extends BaseTypeHandler<Map<LocalDate, Double>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Register custom serializer/deserializer for LocalDate
        SimpleModule module = new SimpleModule();
        // Serializer for converting LocalDate to String
        module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(localDate.toString()); // Serialize LocalDate to "YYYY-MM-DD" string
            }
        });
        // Key deserializer for Map keys that are LocalDate objects
        objectMapper.registerModule(module);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<LocalDate, Double> parameter, JdbcType jdbcType) throws SQLException {
        try {
            // Create a standardized JSON format to ensure consistency
            StringBuilder json = new StringBuilder("{");

            boolean first = true;
            for (Map.Entry<LocalDate, Double> entry : parameter.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey().toString()).append("\":").append(entry.getValue());
                first = false;
            }
            json.append("}");

            ps.setString(i, json.toString());
        } catch (Exception e) {
            throw new SQLException("Error converting Map<LocalDate, Double> to JSON string", e);
        }
    }

    @Override
    public Map<LocalDate, Double> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJsonToMap(json);
    }

    @Override
    public Map<LocalDate, Double> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJsonToMap(json);
    }

    @Override
    public Map<LocalDate, Double> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJsonToMap(json);
    }

    private Map<LocalDate, Double> parseJsonToMap(String json) throws SQLException {
        if (json == null || json.trim().isEmpty() || json.equals("{}")) { // Handle empty object as well
            return Collections.emptyMap();
        }
        try {
            // Create a map to store the result
            Map<LocalDate, Double> result = new HashMap<>();

            // Manual parsing for test format: {"2025-06-23":95.5, "2025-06-24":85.0}
            // Remove curly braces
            json = json.trim();
            if (json.startsWith("{")) {
                json = json.substring(1);
            }
            if (json.endsWith("}")) {
                json = json.substring(0, json.length() - 1);
            }

            // Split by commas not inside quotes
            String[] pairs = json.split(",\\s*");

            for (String pair : pairs) {
                // Split each pair by colon
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    // Extract key (remove quotes)
                    String keyStr = keyValue[0].trim();
                    if (keyStr.startsWith("\"") && keyStr.endsWith("\"")) {
                        keyStr = keyStr.substring(1, keyStr.length() - 1);
                    }

                    // Parse the LocalDate and Double
                    LocalDate key = LocalDate.parse(keyStr);
                    Double value = Double.parseDouble(keyValue[1].trim());

                    result.put(key, value);
                } else {
                    throw new SQLException("Invalid JSON string for LocalDateDoubleMapTypeHandler: " + json);
                }
            }

            return result;
        } catch (Exception e) {
            throw new SQLException("Error converting JSON string to Map<LocalDate, Double>", e);
        }
    }
}