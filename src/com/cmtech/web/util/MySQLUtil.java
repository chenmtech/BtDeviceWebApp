package com.cmtech.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLUtil {
	public static final int INVALID_ID = -1;
	private static final String DBURL = "jdbc:mysql://localhost:3306/btdevice?characterEncoding=utf-8";
	private static final String DBUSER = "root";
	private static final String DBPASSWORD = "ctl080512";
	private static Connection conn = null;
	
	public static Connection getConnection() {
		if(conn == null) {
			connect();
		}
		return conn;
	}
	
	private static void connect() {
		try {
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
}
