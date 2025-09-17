package me.hysong.libcodablejdbc.utils.interfaces;

import me.hysong.libcodablejdbc.utils.exceptions.InitializationViolationException;
import me.hysong.libcodablejdbc.utils.exceptions.JDBCReflectionGeneralException;
import me.hysong.libcodablejdbc.utils.objects.DatabaseElement;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public interface DatabaseTableService {
    Connection getConnection() throws SQLException;
    
    <T> T executeQuery(String sql, Object[] params, ResultSetProcessor<T> resultSetProcessor) throws SQLException, IOException, JDBCReflectionGeneralException, InitializationViolationException;

    int executeUpdate(String sql, Object[] params) throws SQLException, IOException;

    LinkedHashMap<Object, DatabaseElement> select(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException;

    LinkedHashMap<Object, DatabaseElement> selectBy(DatabaseElement blueprint, int offset, int limit, String[] columnNames, Object[] values) throws IOException, SQLException, InitializationViolationException, JDBCReflectionGeneralException;

    int update(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException;

    int insert(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException;

    int delete(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException;
}
