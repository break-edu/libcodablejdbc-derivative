package me.hysong.libcodablejdbc.utils.objects;

import me.hysong.libcodablejdbc.*;
import me.hysong.libcodablejdbc.utils.exceptions.InitializationViolationException;
import me.hysong.libcodablejdbc.utils.exceptions.JDBCReflectionGeneralException;
import me.hysong.libcodablejdbc.utils.interfaces.DatabaseTableService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class DatabaseElement implements RSCodable {

    private boolean isPKInitialized = false;
    private DatabaseTableService controller;

    public DatabaseElement(DatabaseTableService controller) {
        this.controller = controller;
    }

    public Object update() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
        return controller.update(this);
    }

    public Object insert() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
        return controller.insert(this);
    }

    public Object delete() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
        return controller.delete(this);
    }

    public Object select() throws JDBCReflectionGeneralException, SQLException, InitializationViolationException, IOException {
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
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new JDBCReflectionGeneralException(e);
        }
    }

    public LinkedHashMap<String, String> getColumns() {
        LinkedHashMap<String, String> columns = new LinkedHashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!RSCodableUtil.isMarkedMappingElement(field, RSMapping.class)) {
                continue;
            }
            field.setAccessible(true);

            RSMapping annotation = field.getAnnotation(RSMapping.class);
            String fieldSpecName = annotation.mapTo();
            if (fieldSpecName.isEmpty()) {
                fieldSpecName = field.getName();
            }

            columns.put(fieldSpecName, field.getType().getName());
        }
        return columns;
    }

    public LinkedHashMap<String, Object> getValues() throws IllegalAccessException {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!RSCodableUtil.isMarkedMappingElement(field, RSMapping.class)) {
                continue;
            }

            RSMapping annotation = field.getAnnotation(RSMapping.class);
            String fieldSpecName = annotation.mapTo();
            if (fieldSpecName.isEmpty()) {
                fieldSpecName = field.getName();
            }

            field.setAccessible(true);
            values.put(fieldSpecName, field.get(this));
        }
        return values;
    }
}
