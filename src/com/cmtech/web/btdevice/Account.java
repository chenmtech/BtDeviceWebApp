package com.cmtech.web.btdevice;

import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cmtech.web.util.MySQLUtil;

public class Account {
	private final String platName;
	private final String platId;
	
	public Account(String platName, String platId) {
		this.platName = platName;
		this.platId = platId;
	}
	
	public String getPlatName() {
		return platName;
	}

	public String getPlatId() {
		return platId;
	}
	
	public int getId() {
		return getId(platName, platId);
	}

	public static int getId(String platName, String platId) {
		Connection conn = MySQLUtil.connect();		
		if(conn == null) return INVALID_ID;
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rlt = null;
		String sql = "select id from account where platName = ? and platId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, platName);
			ps.setString(2, platId);
			rlt = ps.executeQuery();
			if(rlt.next()) {
				id = rlt.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(rlt != null)
				try {
					rlt.close();
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
			
			MySQLUtil.disconnect(conn);
		}
		return id;		
	}
	
	public boolean insert() {
		Connection conn = MySQLUtil.connect();
		if(conn == null) {
			return false;
		}
		
		PreparedStatement ps = null;
		String sql = "insert into account (platName, platId) values (?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, platName);
			ps.setString(2, platId);
			boolean rlt = ps.execute();
			if(!rlt && ps.getUpdateCount() == 1)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			MySQLUtil.disconnect(conn);
		}
		return false;
	}
	
	public boolean update() {
		Connection conn = MySQLUtil.connect();
		if(conn == null) return false;
		
		int id = getId();
		if(id == INVALID_ID) return false;
		
		PreparedStatement ps = null;
		String sql = "update account set platName=?, platId=? where id=?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, platName);
			ps.setString(2, platId);
			ps.setInt(3, id);
			return ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			MySQLUtil.disconnect(conn);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "platName="+platName+",platId="+platId;
	}
}
