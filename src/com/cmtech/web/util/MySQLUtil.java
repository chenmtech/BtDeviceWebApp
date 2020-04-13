package com.cmtech.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLUtil {
	public static final int INVALID_ID = -1;
	private static Connection conn = null;
	
	public static void connect(String DBURL, String DBUSER, String DBPASSWORD) {
		try {
			if(conn != null) return;
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			conn = null;
		}
	}
	
	public static void disconnect() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				conn = null;
			}
		}
	}
	
	public static Connection getConnection() {
		return conn;
	}
}
