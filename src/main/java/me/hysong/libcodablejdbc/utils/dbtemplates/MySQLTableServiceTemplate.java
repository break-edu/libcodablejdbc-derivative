package me.hysong.libcodablejdbc.utils.dbtemplates;

import me.hysong.libcodablejdbc.utils.exceptions.InitializationViolationException;
import me.hysong.libcodablejdbc.utils.exceptions.JDBCReflectionGeneralException;
import me.hysong.libcodablejdbc.utils.interfaces.DatabaseTableService;
import me.hysong.libcodablejdbc.utils.objects.DatabaseElement;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public interface MySQLTableServiceTemplate extends DatabaseTableService {

    default LinkedHashMap<Object, DatabaseElement> select(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException {
        ResultSet rs = execute(object.getDatabase(), "SELECT * FROM " + object.getTable() + " WHERE " + object.getPrimaryKeyColumnName() + " = ?;", new Object[]{object.getPrimaryKeyValue()});
        return getObjectDatabaseElementLinkedHashMap(object, rs);
    }

    private LinkedHashMap<Object, DatabaseElement> getObjectDatabaseElementLinkedHashMap(DatabaseElement object, ResultSet rs) throws SQLException, InitializationViolationException, JDBCReflectionGeneralException {
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

        ResultSet rs = execute(blueprint.getDatabase(), sb.toString(), values);
        return getObjectDatabaseElementLinkedHashMap(blueprint, rs);
    }

    default ResultSet update(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException {
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

        return execute(object.getDatabase(), sb.toString(), params);
    }

    default ResultSet insert(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException {
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

        return execute(object.getDatabase(), sb.toString(), paramValues);
    }

    default ResultSet delete(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException {
        return execute(object.getDatabase(), "DELETE FROM " + object.getTable() + " WHERE " + object.getPrimaryKeyColumnName() + " = ?;", new Object[]{object.getPrimaryKeyValue()});
    }

}
