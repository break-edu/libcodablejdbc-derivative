package me.hysong.libcodablejdbc.dev_example;

import me.hysong.libcodablejdbc.utils.dbtemplates.MySQLTableServiceTemplate;
import me.hysong.libcodablejdbc.utils.interfaces.DatabaseTableService;
import me.hysong.libcodablejdbc.utils.objects.DatabaseElement;

import java.io.IOException;
import java.sql.*;
import java.util.Map;

public class LocalSQLTableServiceSample implements MySQLTableServiceTemplate {

    private static Connection con;

    public LocalSQLTableServiceSample() {
        // This construction is called very frequently so DO NOT make connection here
    }

    @Override
    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost/?characterEncoding=UTF-8&serverTimezone=UTC";
        String user = "user";
        String passwd = "1234";
        if (con == null || con.isClosed()) {
            con = DriverManager.getConnection(url, user, passwd);
        }
        return con;
    }
}
