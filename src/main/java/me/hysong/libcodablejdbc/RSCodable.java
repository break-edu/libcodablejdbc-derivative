package me.hysong.libcodablejdbc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public interface RSCodable {

    default void objectifyCurrentRow(ResultSet rs) {
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (!RSCodableUtil.isMarkedMappingElement(field)) {
                continue;
            }

            final String columnName = RSCodableUtil.getFieldNameInDB(field);
            final Class<?> fieldType = field.getType();

            try {
                // Skip if the column doesn't exist in the ResultSet
                rs.findColumn(columnName);
            } catch (SQLException missing) {
                // Column not present in this query — skip gracefully
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = null;

                // --- Type-targeted extraction (NULL-safe where relevant) ---
                if (fieldType == String.class) {
                    value = rs.getString(columnName);

                } else if (fieldType == int.class || fieldType == Integer.class) {
                    int v = rs.getInt(columnName);
                    value = rs.wasNull() ? (fieldType.isPrimitive() ? 0 : null) : v;

                } else if (fieldType == long.class || fieldType == Long.class) {
                    long v = rs.getLong(columnName);
                    value = rs.wasNull() ? (fieldType.isPrimitive() ? 0L : null) : v;

                } else if (fieldType == short.class || fieldType == Short.class) {
                    short v = rs.getShort(columnName);
                    value = rs.wasNull() ? (fieldType.isPrimitive() ? (short)0 : null) : v;

                } else if (fieldType == byte.class || fieldType == Byte.class) {
                    byte v = rs.getByte(columnName);
                    value = rs.wasNull() ? (fieldType.isPrimitive() ? (byte)0 : null) : v;

                } else if (fieldType == double.class || fieldType == Double.class) {
                    double v = rs.getDouble(columnName);
                    value = rs.wasNull() ? (fieldType.isPrimitive() ? 0d : null) : v;

                } else if (fieldType == float.class || fieldType == Float.class) {
                    float v = rs.getFloat(columnName);
                    value = rs.wasNull() ? (fieldType.isPrimitive() ? 0f : null) : v;

                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    boolean v = rs.getBoolean(columnName);
                    value = rs.wasNull() ? (fieldType.isPrimitive() ? false : null) : v;

                } else if (fieldType == byte[].class) {
                    value = rs.getBytes(columnName);

                } else if (fieldType == java.math.BigDecimal.class) {
                    value = rs.getBigDecimal(columnName);

                } else if (fieldType == java.math.BigInteger.class) {
                    String s = rs.getString(columnName);
                    value = (s == null) ? null : new java.math.BigInteger(s);

                } else if (fieldType == java.util.Date.class) {
                    java.sql.Timestamp ts = rs.getTimestamp(columnName);
                    value = (ts == null) ? null : new java.util.Date(ts.getTime());

                } else if (fieldType == java.sql.Date.class) {
                    value = rs.getDate(columnName);

                } else if (fieldType == java.sql.Time.class) {
                    value = rs.getTime(columnName);

                } else if (fieldType == java.sql.Timestamp.class) {
                    value = rs.getTimestamp(columnName);

                } else if (fieldType == java.time.LocalDate.class) {
                    java.sql.Date d = rs.getDate(columnName);
                    value = (d == null) ? null : d.toLocalDate();

                } else if (fieldType == java.time.LocalTime.class) {
                    java.sql.Time t = rs.getTime(columnName);
                    value = (t == null) ? null : t.toLocalTime();

                } else if (fieldType == java.time.LocalDateTime.class) {
                    java.sql.Timestamp ts = rs.getTimestamp(columnName);
                    value = (ts == null) ? null : ts.toLocalDateTime();

                } else if (fieldType == java.time.OffsetDateTime.class) {
                    // Best effort: try JDBC 4.2 typed getObject; fallback to ISO-8601 text
                    try {
                        value = rs.getObject(columnName, java.time.OffsetDateTime.class);
                    } catch (Throwable ignore) {
                        String s = rs.getString(columnName);
                        value = (s == null) ? null : java.time.OffsetDateTime.parse(s);
                    }

                } else if (fieldType == java.util.UUID.class) {
                    try {
                        value = rs.getObject(columnName, java.util.UUID.class);
                    } catch (Throwable ignore) {
                        String s = rs.getString(columnName);
                        value = (s == null) ? null : java.util.UUID.fromString(s);
                    }

                } else if (fieldType.isEnum()) {
                    // Prefer string name; if null, try ordinal
                    String name = rs.getString(columnName);
                    if (name != null) {
                        value = Enum.valueOf((Class<? extends Enum>) fieldType.asSubclass(Enum.class), name);
                    } else {
                        int ord = rs.getInt(columnName);
                        if (!rs.wasNull()) {
                            Object[] constants = fieldType.getEnumConstants();
                            if (ord >= 0 && ord < constants.length) {
                                value = constants[ord];
                            }
                        }
                    }

                } else {
                    // Last resort: JDBC 4.2 typed getObject; if unsupported, plain getObject
                    try {
                        value = rs.getObject(columnName, fieldType);
                    } catch (Throwable ignore) {
                        value = rs.getObject(columnName);
                    }
                }

                // Assign if not null OR field is primitive (needs a default already handled)
                if (value != null || fieldType.isPrimitive()) {
                    field.set(this, value);
                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to read column '" + columnName + "' from ResultSet", e);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new RuntimeException("Failed to set field '" + field.getName() + "' on " + this.getClass().getName(), e);
            }
        }
    }


}
