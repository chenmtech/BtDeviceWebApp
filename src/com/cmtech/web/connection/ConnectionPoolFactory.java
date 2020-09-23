package com.cmtech.web.connection;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPoolFactory {
	private static ConnectionPool connectionPool;
	
	private ConnectionPoolFactory() {
		
	}
    
    public static void init(String jdbcDriver, String dbUrl, String userName, String password) {
    	if(connectionPool == null) {
    		connectionPool = new ConnectionPool(jdbcDriver, dbUrl, userName, password);
    	}
    }
 
    public static Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }
 
    public static void returnConnection(Connection connection){
        connectionPool.returnConnection(connection);
    }
 
    public static void refreshConnection() throws SQLException {
        connectionPool.refreshConnection();
    }
 
    public static void closeConnectionPool() throws SQLException {
        connectionPool.closeConnectionPool();
    }
 
    public static void setMaxConnections(int maxConnections) {
        connectionPool.setMaxConnections(maxConnections);
    }
 
    public void setIncrementalConnections(int incrementalConnections) {
        connectionPool.setIncrementalConnections(incrementalConnections);
    }
 
    static ConnectionPool getConnectionPool() {
        return connectionPool;
    }

}
