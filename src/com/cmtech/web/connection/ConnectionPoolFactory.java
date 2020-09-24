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
    	if(connectionPool == null) return null;
        return connectionPool.getConnection();
    }
 
    public static void returnConnection(Connection connection){
    	if(connectionPool != null)
    		connectionPool.returnConnection(connection);
    }
 
    public static void refreshConnection() throws SQLException {
    	if(connectionPool != null)
    		connectionPool.refreshConnection();
    }
 
    public static void closeConnectionPool() throws SQLException {
    	if(connectionPool != null)
    		connectionPool.closeConnectionPool();
    }
 
    public static void setMaxConnections(int maxConnections) {
    	if(connectionPool != null)
    		connectionPool.setMaxConnections(maxConnections);
    }
 
    public void setIncrementalConnections(int incrementalConnections) {
    	if(connectionPool != null)
    		connectionPool.setIncrementalConnections(incrementalConnections);
    }
 
    static ConnectionPool getConnectionPool() {
        return connectionPool;
    }

}
