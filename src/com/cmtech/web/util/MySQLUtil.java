package com.cmtech.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLUtil {
	public static final int INVALID_ID = -1;
	private static final String DBURL = "jdbc:mysql://localhost:3306/btdevice";
	private static final String USER = "root";
	private static final String PASSWORD = "ctl080512";
	private static Connection conn = null;
	
	public static void connect() {
		try {
			if(conn != null) return;
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DBURL, USER, PASSWORD);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
	}
	
	public static void disconnect() {
		if(conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Connection getConnection() {
		return conn;
	}
}
