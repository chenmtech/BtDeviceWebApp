package com.cmtech.web.dbUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUtil {
	public static final int INVALID_ID = -1;
	private static final String DBNAME = "com.mysql.cj.jdbc.Driver";
	private static String dbAddress = "localhost:3306";
	private static String dbUser = "root";
	private static String dbPassword = "ctl080512";
	
	public static void setDbAddress(String dbAddress) {
		DbUtil.dbAddress = dbAddress; 
	}
	
	public static String getDbUrl() {
		return "jdbc:mysql://" + dbAddress + "/btdevice?characterEncoding=utf-8";
	}
	
	public static void setUserInfo(String name, String password) {
		dbUser = name;
		dbPassword = password;
	}
	
	public static Connection connect() {
		try {
			Class.forName(DBNAME);
			Connection conn = DriverManager.getConnection(getDbUrl(), dbUser, dbPassword);
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
	
	public static void closeRS(ResultSet rs) {
		if(rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void closePS(PreparedStatement ps) {
		if(ps != null)
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void close(ResultSet rs, PreparedStatement ps, Connection conn) {
		if(rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		if(ps != null)
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
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
