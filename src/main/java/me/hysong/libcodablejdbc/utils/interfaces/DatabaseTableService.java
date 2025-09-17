package me.hysong.libcodablejdbc.utils.interfaces;

import me.hysong.libcodablejdbc.utils.exceptions.InitializationViolationException;
import me.hysong.libcodablejdbc.utils.exceptions.JDBCReflectionGeneralException;
import me.hysong.libcodablejdbc.utils.objects.DatabaseElement;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public interface DatabaseTableService {

    void setAuthorization(String database, Map<String, Object> authorizationData);
    
    ResultSet execute(String database, String sql, Object[] params) throws SQLException, IOException;

    LinkedHashMap<Object, DatabaseElement> select(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException;

    LinkedHashMap<Object, DatabaseElement> selectBy(DatabaseElement blueprint, int offset, int limit, String[] columnNames, Object[] values) throws IOException, SQLException, InitializationViolationException, JDBCReflectionGeneralException;

    ResultSet update(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException;

    ResultSet insert(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException;

    ResultSet delete(DatabaseElement object) throws InitializationViolationException, JDBCReflectionGeneralException, SQLException, IOException;
}
