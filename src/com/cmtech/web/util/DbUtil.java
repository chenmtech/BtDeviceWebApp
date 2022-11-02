package com.cmtech.web.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.cmtech.web.connection.ConnectionPoolFactory;

public class DbUtil {
	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String KM_TENGXUN_CLOUD_ADDRESS = "203.195.137.198:3306";
	private static final String DEFAULT_USER_NAME = "webuser";
	private static final String DEFAULT_PASSWORD = "chenm740216";
	
	
	private static String dbDriver = JDBC_DRIVER;
	private static String dbAddress = KM_TENGXUN_CLOUD_ADDRESS;
	private static String dbUserName = DEFAULT_USER_NAME;
	private static String dbPassword = DEFAULT_PASSWORD;
	
	public static void setDbDriver(String driver) {
		dbDriver = driver;
	}
	
	public static void setDbAddress(String address) {
		DbUtil.dbAddress = address; 
	}
	
	public static void setDbUser(String userName, String password) {
		dbUserName = userName;
		dbPassword = password;
	}
	
	public static String getDbUrl() {
		return "jdbc:mysql://" + dbAddress + "/btdevice?characterEncoding=utf-8";
	}
	
	public static Connection connect() {
		ConnectionPoolFactory.init(dbDriver, getDbUrl(), dbUserName, dbPassword);
		try {
			return ConnectionPoolFactory.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void disconnect(Connection conn) {
		ConnectionPoolFactory.returnConnection(conn);
	}
	
	public static void closeRS(ResultSet rs) {
		if(rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void closeSTMT(Statement ps) {
		if(ps != null)
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void close(ResultSet rs, Statement stmt, Connection conn) {
		closeRS(rs);
		closeSTMT(stmt);
		if(conn!=null)
			disconnect(conn);
	}
}
