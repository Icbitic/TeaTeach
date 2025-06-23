package org.bedrock.teateach.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.Arrays;

/**
 * Custom TypeHandler for storing and retrieving int[] arrays to/from the database.
 * This handler uses PostgreSQL's integer array type or serializes to a string 
 * for other database types.
 */
@MappedTypes(int[].class)
public class IntArrayTypeHandler extends BaseTypeHandler<int[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, int[] parameter, JdbcType jdbcType) throws SQLException {
        // Try PostgreSQL array type first
        try {
            // For PostgreSQL, we can use the native array type
            Connection conn = ps.getConnection();
            if (conn.getMetaData().getDatabaseProductName().equals("PostgreSQL")) {
                Array array = conn.createArrayOf("integer", Arrays.stream(parameter).boxed().toArray());
                ps.setArray(i, array);
                return;
            }
        } catch (Exception e) {
            // Fall back to string serialization if PostgreSQL array type is not available
        }

        // Fallback: Convert to comma-separated string
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < parameter.length; j++) {
            if (j > 0) {
                sb.append(',');
            }
            sb.append(parameter[j]);
        }
        ps.setString(i, sb.toString());
    }

    @Override
    public int[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseResult(rs.getObject(columnName));
    }

    @Override
    public int[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseResult(rs.getObject(columnIndex));
    }

    @Override
    public int[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseResult(cs.getObject(columnIndex));
    }

    private int[] parseResult(Object result) {
        if (result == null) {
            return new int[0];
        }

        // If PostgreSQL returned an array
        if (result instanceof Array) {
            try {
                Object array = ((Array) result).getArray();
                if (array instanceof Integer[]) {
                    Integer[] objArray = (Integer[]) array;
                    return Arrays.stream(objArray).mapToInt(Integer::intValue).toArray();
                } else if (array instanceof int[]) {
                    return (int[]) array;
                }
            } catch (SQLException e) {
                return new int[0];
            }
        }

        // If it's a string (our comma-separated format)
        if (result instanceof String) {
            String str = (String) result;
            if (str.isEmpty()) {
                return new int[0];
            }

            String[] parts = str.split(",");
            int[] array = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    array[i] = Integer.parseInt(parts[i].trim());
                } catch (NumberFormatException e) {
                    array[i] = 0;
                }
            }
            return array;
        }

        return new int[0];
    }
}
