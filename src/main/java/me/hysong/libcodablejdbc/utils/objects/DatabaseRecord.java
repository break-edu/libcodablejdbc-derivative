package me.hysong.libcodablejdbc.utils.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import me.hysong.libcodablejdbc.*;
import me.hysong.libcodablejdbc.utils.exceptions.InitializationViolationException;
import me.hysong.libcodablejdbc.utils.exceptions.JDBCReflectionGeneralException;
import me.hysong.libcodablejdbc.utils.interfaces.DatabaseTableService;
import me.hysong.libcodablejson.JsonCodable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public abstract class DatabaseRecord implements RSCodable {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Getter private boolean isPKInitialized = false;
    @Getter private final DatabaseTableService controller;

    public DatabaseRecord(DatabaseTableService controller) {
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

    public LinkedHashMap <Object, DatabaseRecord> selectAll() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
        return controller.selectAll(this);
    }

    public void select() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException, IllegalAccessException {
        LinkedHashMap<Object, DatabaseRecord> selected = selectAll();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        Object firstIndex = selected.sequencedKeySet().getFirst();
        DatabaseRecord loaded = selected.get(firstIndex);

        for (Field field : loaded.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            field.set(this, field.get(loaded));
        }
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
            f.setAccessible(true);
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
            if (field.getType().isAssignableFrom(CompositionObject.class)) {
                CompositionObject compositionObject;
                try {
                    compositionObject = (CompositionObject) field.get(this);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                compositionObject.setPrefix(field.getName());
                HashMap<String, Object> keys = compositionObject.decompose();
                for (String key : keys.keySet()) {
                    columns.put(key, keys.get(key).getClass().getName());
                }
            } else {
                columns.put(RSCodableUtil.getFieldNameInDB(field), field.getType().getName());
            }
        }
        return columns;
    }

    public ArrayList<String> getColumnNames() {
        ArrayList<String> columnNames = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!RSCodableUtil.isMarkedMappingElement(field)) {
                continue;
            }
            field.setAccessible(true);
            if (field.getType().isAssignableFrom(CompositionObject.class)) {
                CompositionObject compositionObject;
                try {
                    compositionObject = (CompositionObject) field.get(this);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                compositionObject.setPrefix(field.getName());
                String[] keys = compositionObject.compositionKeys();
                Collections.addAll(columnNames, keys);
                continue;
            }
            columnNames.add(RSCodableUtil.getFieldNameInDB(field));
        }
        return columnNames;
    }

    public LinkedHashMap<String, Object> getValues() throws IllegalAccessException {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!RSCodableUtil.isMarkedMappingElement(field)) {
                continue;
            }

            field.setAccessible(true);
            if (field.getType().isAssignableFrom(CompositionObject.class)) {
                CompositionObject compositionObject = (CompositionObject) field.get(this);
                compositionObject.setPrefix(field.getName());
                HashMap<String, Object> decomposed = compositionObject.decompose();
                for (String key : decomposed.keySet()) {
                    values.put(key, decomposed.get(key));
                }
                continue;
            }
            values.put(RSCodableUtil.getFieldNameInDB(field), field.get(this));
        }
        return values;
    }

    public static PreparedStatement setObjects(PreparedStatement preparedStatement, Object... parameters) {
        try {
            for (int i = 0; i < parameters.length; i++) {
                int parameterIndex = i + 1;
                Object param = parameters[i];
                if (param == null) {
                    preparedStatement.setNull(parameterIndex, Types.NULL);
                } else if (param instanceof String s) {
                    preparedStatement.setString(parameterIndex, s);
                } else if (param instanceof Integer integer) {
                    preparedStatement.setInt(parameterIndex, integer);
                } else if (param instanceof Long l) {
                    preparedStatement.setLong(parameterIndex, l);
                } else if (param instanceof Double v) {
                    preparedStatement.setDouble(parameterIndex, v);
                } else if (param instanceof BigDecimal bigDecimal) {
                    preparedStatement.setBigDecimal(parameterIndex, bigDecimal);
                } else if (param instanceof Boolean b) {
                    preparedStatement.setBoolean(parameterIndex, b);
                } else if (param instanceof LocalDate localDate) {
                    preparedStatement.setObject(parameterIndex, localDate);
                } else if (param instanceof LocalDateTime localDateTime) {
                    preparedStatement.setObject(parameterIndex, localDateTime);
                } else if (param instanceof Timestamp timestamp) {
                    preparedStatement.setTimestamp(parameterIndex, timestamp);
                } else if (param instanceof java.sql.Date date) {
                    preparedStatement.setDate(parameterIndex, date);
                } else if (param instanceof List || param instanceof Map) {
                    try {
                        String jsonString = objectMapper.writeValueAsString(param);
                        preparedStatement.setString(parameterIndex, jsonString);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize object to JSON for PreparedStatement", e);
                    }
                } else if (param.getClass().isAssignableFrom(JsonCodable.class)) {
                    String jsonString = ((JsonCodable) param).toJsonString();
                    preparedStatement.setString(parameterIndex, jsonString);
                } else if (param.getClass().isAssignableFrom(CompositionObject.class)) {
                    try {
                        LinkedHashMap<String, Object> decomposed = ((CompositionObject) param).decompose();
                        String jsonString = objectMapper.writeValueAsString(decomposed);
                        preparedStatement.setString(parameterIndex, jsonString);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize CompositionObject to JSON for PreparedStatement", e);
                    }
                } else {
                    preparedStatement.setObject(parameterIndex, param);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set PreparedStatement parameters", e);
        }
        return preparedStatement;
    }

    public boolean buildTable() {
        try {
            return buildTable(false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean buildTable(boolean useThrow) throws SQLException {
        // Get @Database annotation
        if (!this.getClass().isAnnotationPresent(Database.class)) {
            throw new RuntimeException("Database is not configured using @Database(db=...) annotation.");
        }
        Database dbAnnotation = this.getClass().getAnnotation(Database.class);
        String dbName = dbAnnotation.db();
        String tableName = dbAnnotation.table();
        String query = "CREATE TABLE IF NOT EXISTS " + dbName + "." + tableName + " (";
        ArrayList<String> columnDefs = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!RSCodableUtil.isMarkedMappingElement(field)) {
                continue;
            }
            String columnName = RSCodableUtil.getFieldNameInDB(field);
            String sqlType = RSCodableUtil.mapJavaTypeToSQLType(field.getType());
            String columnDef = columnName + " " + sqlType;
            if (this.getClass().isAnnotationPresent(PrimaryKey.class)) {
                PrimaryKey pkAnnotation = this.getClass().getAnnotation(PrimaryKey.class);
                if (pkAnnotation.column().equals(columnName)) {
                    columnDef += " PRIMARY KEY";
                }
            }
            columnDefs.add(columnDef);
        }

        query += String.join(", ", columnDefs) + ");";
        System.out.println("DEBUG: Statement: \"" + query + "\"");
        try (Connection conn = controller.getConnection(dbName); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            if (useThrow) {
                throw e;
            } else {
                return false;
            }
        }
    }

    public void deepFetch(int depth) {
         // Iterate through fields with @ForeignKey or @ForeignKeyList annotation
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ForeignKeyList.class)) {
                ForeignKeyList fkListAnnotation = field.getAnnotation(ForeignKeyList.class);
                Class<? extends DatabaseRecord> foreignClass = fkListAnnotation.type();
                String foreignKeyColumn = fkListAnnotation.reference();
                String assignTo = fkListAnnotation.assignTo();

                // Current field is expected to have ArrayList<?> type which is list of foreign keys
                field.setAccessible(true);
                try {
                    Object foreignKeyListObj = field.get(this);
                    if (!(foreignKeyListObj instanceof ArrayList<?> foreignKeyList)) {
                        continue; // Skip if not an ArrayList
                    }

                    // Prepare to collect fetched foreign records
                    ArrayList<DatabaseRecord> fetchedRecords = new ArrayList<>();

                    // For each foreign key, fetch the corresponding record
                    for (Object foreignKeyValue : foreignKeyList) {
                        DatabaseRecord foreignRecordInstance = foreignClass.getDeclaredConstructor(DatabaseTableService.class)
                                .newInstance(this.controller);

                        // Set the foreign key value to the appropriate field
                        Field foreignField = foreignClass.getDeclaredField(foreignKeyColumn);
                        foreignField.setAccessible(true);
                        foreignField.set(foreignRecordInstance, foreignKeyValue);
                        // Fetch the full foreign record
                        foreignRecordInstance.select();
                        // If depth > 1, recursively deep fetch
                        if (depth > 1) {
                            foreignRecordInstance.deepFetch(depth - 1);
                        }
                        fetchedRecords.add(foreignRecordInstance);
                    }
                    // Assign fetched records to the designated field
                    if (!assignTo.isEmpty()) {
                        Field assignField = this.getClass().getDeclaredField(assignTo);
                        assignField.setAccessible(true);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else if (field.isAnnotationPresent(ForeignKey.class)) {
                ForeignKey fkAnnotation = field.getAnnotation(ForeignKey.class);
                Class<? extends DatabaseRecord> foreignClass = fkAnnotation.type();
                String foreignKeyColumn = fkAnnotation.reference();
                String assignTo = fkAnnotation.assignTo();

                // Current field is expected to have the foreign key value
                field.setAccessible(true);
                try {
                    Object foreignKeyValue = field.get(this);
                    if (foreignKeyValue == null) {
                        continue; // Skip if foreign key value is null
                    }

                    // Create an instance of the foreign record
                    DatabaseRecord foreignRecordInstance = foreignClass.getDeclaredConstructor(DatabaseTableService.class)
                            .newInstance(this.controller);

                    // Set the foreign key value to the appropriate field
                    Field foreignField = foreignClass.getDeclaredField(foreignKeyColumn);
                    foreignField.setAccessible(true);
                    foreignField.set(foreignRecordInstance, foreignKeyValue);
                    // Fetch the full foreign record
                    foreignRecordInstance.select();
                    // If depth > 1, recursively deep fetch
                    if (depth > 1) {
                        foreignRecordInstance.deepFetch(depth - 1);
                    }
                    // Assign fetched record to the designated field
                    if (!assignTo.isEmpty()) {
                        Field assignField = this.getClass().getDeclaredField(assignTo);
                        assignField.setAccessible(true);
                        assignField.set(this, foreignRecordInstance);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void deepFetch() {
        deepFetch(1);
    }

}
