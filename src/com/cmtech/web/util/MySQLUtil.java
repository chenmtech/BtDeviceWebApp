package com.cmtech.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLUtil {
	public static final int INVALID_ID = -1;
	private static final String DBNAME = "com.mysql.cj.jdbc.Driver";
	private static final String DBURL = "jdbc:mysql://localhost:3306/btdevice?characterEncoding=utf-8";
	private static final String DBUSER = "root";
	private static final String DBPASSWORD = "ctl080512";
	
	public static Connection connect() {
		try {
			Class.forName(DBNAME);
			Connection conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);
			return conn;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void disconnect(Connection conn) {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
