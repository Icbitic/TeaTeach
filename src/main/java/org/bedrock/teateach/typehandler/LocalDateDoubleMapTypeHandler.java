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
 * Requires custom serializers/deserializers for LocalDate.
 */
@MappedJdbcTypes(JdbcType.VARCHAR) // Maps to VARCHAR/TEXT in DB
@MappedTypes(Map.class) // Maps Java Map
public class LocalDateDoubleMapTypeHandler extends BaseTypeHandler<Map<LocalDate, Double>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Register custom serializer/deserializer for LocalDate
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(localDate.toString()); // Serialize LocalDate to "YYYY-MM-DD" string
            }
        });
        module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                try {
                    return LocalDate.parse(jsonParser.getText()); // Deserialize "YYYY-MM-DD" string to LocalDate
                } catch (DateTimeParseException e) {
                    throw new IOException("Failed to parse LocalDate: " + jsonParser.getText(), e);
                }
            }
        });
        objectMapper.registerModule(module);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<LocalDate, Double> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
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
            // Need a specific TypeReference for Map<LocalDate, Double> due to type erasure
            return objectMapper.readValue(json, new TypeReference<Map<LocalDate, Double>>() {});
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting JSON string to Map<LocalDate, Double>", e);
        }
    }
}