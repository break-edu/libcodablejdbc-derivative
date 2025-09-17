package me.hysong.libcodablejdbc.utils.objects;

import me.hysong.libcodablejdbc.*;
import me.hysong.libcodablejdbc.utils.exceptions.InitializationViolationException;
import me.hysong.libcodablejdbc.utils.exceptions.JDBCReflectionGeneralException;
import me.hysong.libcodablejdbc.utils.interfaces.DatabaseTableService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public abstract class DatabaseElement implements RSCodable {

    private boolean isPKInitialized = false;
    private final DatabaseTableService controller;

    public DatabaseElement(DatabaseTableService controller) {
        this.controller = controller;
    }

    public int update() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
        return controller.update(this);
    }

    public int insert() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
        return controller.insert(this);
    }

    public int delete() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
        return controller.delete(this);
    }

    public LinkedHashMap <Object, DatabaseElement> select() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
        return controller.select(this);
    }

    public String getDatabase() throws InitializationViolationException{
        if (!this.getClass().isAnnotationPresent(Database.class)) {
            throw new InitializationViolationException("Database is not configured using @Database(db=...) annotation.");
        }
        Database annotation = this.getClass().getAnnotation(Database.class);
        return annotation.db();
    }

    public String getTable() throws InitializationViolationException {
        if (!this.getClass().isAnnotationPresent(Database.class)) {
            throw new InitializationViolationException("Table is not configured using @Database(table=...) annotation.");
        }
        Database annotation = this.getClass().getAnnotation(Database.class);
        return annotation.table();
    }

    public String getPrimaryKeyColumnName() throws InitializationViolationException{
        if (!this.getClass().isAnnotationPresent(PrimaryKey.class)) {
            throw new InitializationViolationException("Primary key is not configured using @PrimaryKey annotation.");
        }
        PrimaryKey annotation = this.getClass().getAnnotation(PrimaryKey.class);
        return annotation.column();
    }

    public Object getPrimaryKeyValue() throws InitializationViolationException, JDBCReflectionGeneralException {
        if (!this.getClass().isAnnotationPresent(PrimaryKey.class)) {
            throw new InitializationViolationException("Primary key is not configured using @PrimaryKey annotation.");
        }
        PrimaryKey annotation = this.getClass().getAnnotation(PrimaryKey.class);
        try {
            Field f = this.getClass().getDeclaredField(annotation.column());
            return f.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new JDBCReflectionGeneralException(e);
        }
    }

    public void setPrimaryKeyValue(Object value) throws InitializationViolationException, JDBCReflectionGeneralException {
        if (isPKInitialized) {
            throw new InitializationViolationException("Primary key is already initialized.");
        }
        if (!this.getClass().isAnnotationPresent(PrimaryKey.class)) {
            throw new InitializationViolationException("Primary key is not configured using @PrimaryKey annotation.");
        }
        PrimaryKey annotation = this.getClass().getAnnotation(PrimaryKey.class);
        try {
            Field f = this.getClass().getDeclaredField(annotation.column());
            f.setAccessible(true);
            f.set(this, value);
            isPKInitialized = true;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new JDBCReflectionGeneralException(e);
        }
    }

    public LinkedHashMap<String, String> getColumns() {
        LinkedHashMap<String, String> columns = new LinkedHashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!RSCodableUtil.isMarkedMappingElement(field)) {
                continue;
            }
            field.setAccessible(true);
            columns.put(RSCodableUtil.getFieldNameInDB(field), field.getType().getName());
        }
        return columns;
    }

    public LinkedHashMap<String, Object> getValues() throws IllegalAccessException {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!RSCodableUtil.isMarkedMappingElement(field)) {
                continue;
            }

            field.setAccessible(true);
            values.put(RSCodableUtil.getFieldNameInDB(field), field.get(this));
        }
        return values;
    }

    public static PreparedStatement setObjects(PreparedStatement preparedStatement, Object... parameters) {
        try {
            // PreparedStatement parameters are 1-indexed, so we start i at 1.
            for (int i = 0; i < parameters.length; i++) {
                int parameterIndex = i + 1;
                Object param = parameters[i];

                switch (param) {
                    case null ->
                        // Handle null values by setting the SQL type to NULL.
                            preparedStatement.setNull(parameterIndex, Types.NULL);
                    case String s -> preparedStatement.setString(parameterIndex, s);
                    case Integer integer -> preparedStatement.setInt(parameterIndex, integer);
                    case Long l -> preparedStatement.setLong(parameterIndex, l);
                    case Double v -> preparedStatement.setDouble(parameterIndex, v);
                    case BigDecimal bigDecimal -> preparedStatement.setBigDecimal(parameterIndex, bigDecimal);
                    case Boolean b -> preparedStatement.setBoolean(parameterIndex, b);
                    case LocalDate localDate -> preparedStatement.setObject(parameterIndex, localDate);
                    case LocalDateTime localDateTime -> preparedStatement.setObject(parameterIndex, localDateTime);
                    case Timestamp timestamp -> preparedStatement.setTimestamp(parameterIndex, timestamp);
                    case java.sql.Date date -> preparedStatement.setDate(parameterIndex, date);

                    // TODO Experimental
                    case ArrayList<?> x -> preparedStatement.setArray(parameterIndex, (Array) x);
                    case LinkedList<?> x -> preparedStatement.setArray(parameterIndex, (Array) x);
                    case List<?> x -> preparedStatement.setArray(parameterIndex, (Array) x);
//                    case LinkedHashMap<?, ?> x -> preparedStatement.setArray(parameterIndex, mapTypeToArrayType(x));
//                    case HashMap<?, ?> x -> preparedStatement.setArray(parameterIndex, mapTypeToArrayType(x));
//                    case Map<?, ?> x -> preparedStatement.setArray(parameterIndex, mapTypeToArrayType(x));

                    default ->
                        // As a fallback, use setObject for any other type.
                            preparedStatement.setObject(parameterIndex, param);
                }
            }
        } catch (SQLException e) {
            // It's good practice to wrap the checked SQLException in an unchecked
            // RuntimeException to avoid cluttering the calling code with try-catch blocks.
            throw new RuntimeException("Failed to set PreparedStatement parameters", e);
        }
        return preparedStatement;
    }
}
