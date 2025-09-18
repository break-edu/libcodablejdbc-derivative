package me.hysong.libcodablejdbc.utils.dbtemplates;

import me.hysong.libcodablejdbc.utils.exceptions.InitializationViolationException;
import me.hysong.libcodablejdbc.utils.exceptions.JDBCReflectionGeneralException;
import me.hysong.libcodablejdbc.utils.interfaces.DatabaseTableService;
import me.hysong.libcodablejdbc.utils.interfaces.ResultSetProcessor;
import me.hysong.libcodablejdbc.utils.objects.DatabaseElement;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public interface MySQLTableServiceTemplate extends DatabaseTableService {

    default LinkedHashMap<Object, DatabaseElement> selectAll(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException {
        return executeQuery(object.getDatabase(), "SELECT * FROM " + object.getTable() + " WHERE " + object.getPrimaryKeyColumnName() + " = ?;", new Object[]{object.getPrimaryKeyValue()}, rs -> getObjectDatabaseElementLinkedHashMap(rs, object));
    }

    private LinkedHashMap<Object, DatabaseElement> getObjectDatabaseElementLinkedHashMap(ResultSet rs, DatabaseElement object) throws SQLException, InitializationViolationException, JDBCReflectionGeneralException {
        LinkedHashMap<Object, DatabaseElement> result = new LinkedHashMap<>();
        Class<?> cz = object.getClass();
        while (rs.next()) {
            try {
                DatabaseElement obj = (DatabaseElement) cz.getDeclaredConstructor().newInstance();
                obj.objectifyCurrentRow(rs);
                result.put(obj.getPrimaryKeyValue(), obj);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new JDBCReflectionGeneralException("Expected constructor (Public, no parameter) not found for class " + cz.getName());
            }
        }
        return result;
    }

    default <T> T executeQuery(String database, String sql, Object[] params, ResultSetProcessor<T> resultSetProcessor) throws SQLException, JDBCReflectionGeneralException, InitializationViolationException {
        Connection con = getConnection(database);
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            DatabaseElement.setObjects(preparedStatement, params);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return resultSetProcessor.process(rs);
            }
        }
    }

    default int executeUpdate(String database, String sql, Object[] params) throws SQLException {
        Connection con = getConnection(database);
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            DatabaseElement.setObjects(preparedStatement, params);
            return preparedStatement.executeUpdate();
        }
    }

    default LinkedHashMap<Object, DatabaseElement> selectBy(DatabaseElement blueprint, int offset, int limit, String[] columnNames, Object[] values) throws IOException, SQLException, InitializationViolationException, JDBCReflectionGeneralException {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(blueprint.getTable()).append(" WHERE ");
        for (int i = 0; i < columnNames.length; i++) {
            sb.append(columnNames[i]).append(" = ?");
            if (i < columnNames.length - 1) {
                sb.append(" AND ");
            }
        }

        if (limit > 0) sb.append(" LIMIT ?");
        if (offset > 0) sb.append(" OFFSET ?");

        sb.append(";");

        return executeQuery(blueprint.getDatabase(), sb.toString(), values, rs -> getObjectDatabaseElementLinkedHashMap(rs, blueprint));
    }

    default int update(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(object.getTable()).append(" SET ");

        LinkedHashMap<String, Object> values;
        try {
            values = object.getValues();
        } catch (IllegalAccessException e) {
            throw new JDBCReflectionGeneralException(e);
        }

        Object[] params = new Object[values.size() + 1];
        int accessed = 0;
        for (String key : values.keySet()) {
            sb.append(key).append(" = ?");
            params[accessed++] = values.get(key);
        }

        sb.append(" WHERE ").append(object.getPrimaryKeyColumnName()).append(" = ?;");
        params[accessed] = object.getPrimaryKeyValue();

        return executeUpdate(object.getDatabase(), sb.toString(), params);
    }

    default int insert(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(object.getTable()).append(" (");
        LinkedHashMap<String, Object> values;
        try {
            values = object.getValues();
        } catch (IllegalAccessException e) {
            throw new JDBCReflectionGeneralException(e);
        }

        Object[] paramValues = new Object[values.size()];
        int iterationCount = 0;
        for (String key : values.keySet()) {
            sb.append(key);
            if (iterationCount < values.size() - 1) {
                sb.append(", ");
            }
            paramValues[iterationCount++] = values.get(key);
        }

        sb.append(") VALUES (");
        for (int i = 0; i < paramValues.length; i++) {
            sb.append("?");
            if (i < paramValues.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(");");

        return executeUpdate(object.getDatabase(), sb.toString(), paramValues);
    }

    default int delete(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException {
        return executeUpdate(object.getDatabase(), "DELETE FROM " + object.getTable() + " WHERE " + object.getPrimaryKeyColumnName() + " = ?;", new Object[]{object.getPrimaryKeyValue()});
    }

}
