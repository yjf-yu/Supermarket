package Util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * 工具类
 */
public class DBUtil{
    private static Logger log = LogManager.getLogger (DBUtil.class.getName ());

    private static final DBUtil dbutil = new DBUtil();

    private final String URL = "jdbc:mysql://localhost:3306/supermarket";
    private final String USER = "root";
    private final String PASSWORD = "root";
    private final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private DBUtil(){
        try {
            Class.forName (DRIVER);
        } catch (ClassNotFoundException e) {
            log.error ("类加载驱动异常", e);
            throw new ExceptionInInitializerError (e);
        }
    }

    public final static DBUtil getDbUtil(){
        return dbutil;
    }


    /**
     * 返回数据库连接
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException{
        return DriverManager.getConnection (URL, USER, PASSWORD);
    }

    /**
     * 释放资源
     *
     * @param st
     * @param conn
     */
    public void close(Statement st, Connection conn){
        if (st != null) {
            try {
                st.close ();
            } catch (SQLException e) {
                log.error ("数据库异常", e);
            }
        }
        if (conn != null) {
            try {
                conn.close ();
            } catch (SQLException e) {
                log.error ("数据库异常", e);
            }
        }

    }

    /**
     * 释放资源
     *
     * @param re
     * @param st
     * @param conn
     */
    public void close(ResultSet re, Statement st, Connection conn){
        if (re != null) {
            try {
                re.close ();
            } catch (SQLException e) {
                log.error ("数据库异常", e);
            }
        }

        close (st, conn);
    }
}
