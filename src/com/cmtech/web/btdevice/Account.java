package com.cmtech.web.btdevice;

import static dbUtil.DbUtil.INVALID_ID;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.util.Base64;

import dbUtil.DbUtil;

public class Account {
	private final String platName;
	private final String platId;
	private final String name;
	private final String note;
	private final byte[] iconData;
	
	public Account(String platName, String platId) {
		this(platName, platId, "", "", null);
	}
	
	public Account(String platName, String platId, String name, String note, byte[] iconData) {
		this.platName = platName;
		this.platId = platId;
		this.name = name;
		this.note = note;
		this.iconData = iconData;
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
	
	public static Account create(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null || id == INVALID_ID) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select platName, platId, name, note, icon from account where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				String platName = rs.getString("platName");
				String platId = rs.getString("platId");
				String name = rs.getString("name");
				String note = rs.getString("note");
				Blob b = rs.getBlob("icon");
				byte[] iconData;
				if(b == null || b.length() < 1) 
					iconData = null;
				else
					iconData = b.getBytes(1, (int)b.length());
				//String iconStr = Base64.encodeToString(iconData, Base64.DEFAULT);
				return new Account(platName, platId, name, note, iconData);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;		
	}

	public static int getId(String platName, String platId) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return INVALID_ID;
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from account where platName = ? and platId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, platName);
			ps.setString(2, platId);
			rs = ps.executeQuery();
			if(rs.next()) {
				id = rs.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return id;		
	}
	
	public boolean insert() {
		Connection conn = DbUtil.connect();
		if(conn == null) {
			return false;
		}
		
		PreparedStatement ps = null;
		String sql = "insert into account (platName, platId, name, note, icon) values (?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, platName);
			ps.setString(2, platId);
			ps.setString(3, name);
			ps.setString(4, note);
			//byte[] iconData = Base64.decode(iconStr, Base64.DEFAULT);
			Blob b = conn.createBlob();
			b.setBytes(1, iconData);
			ps.setBlob(5, b);
			boolean rlt = ps.execute();
			if(!rlt && ps.getUpdateCount() == 1)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	public boolean updateDb() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		int id = getId();
		if(id == INVALID_ID) return false;
		
		PreparedStatement ps = null;
		String sql = "update account set name=?, note=?, icon=? where id=?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, note);
			//byte[] iconData = Base64.decode(iconStr, Base64.DEFAULT);
			Blob b = conn.createBlob();
			b.setBytes(1, iconData);
			ps.setBlob(3, b);
			ps.setInt(4, id);
			boolean rlt = ps.execute();
			if(!rlt && ps.getUpdateCount() == 1)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("platName", platName);
		json.put("platId", platId);
		json.put("name", name);
		json.put("note", note);
		if(iconData == null)
			json.put("iconStr", "");
		else
			json.put("iconStr", Base64.encodeToString(iconData, Base64.DEFAULT));
	
		return json;
	}
	
	@Override
	public String toString() {
		return "platName="+platName+",platId="+platId+",name="+name+",note="+note+",iconData="+iconData;
	}
	
	public boolean login() {
		return (getId() != INVALID_ID);
	}
	
	public boolean signUp() {
		return (getId() == INVALID_ID && insert());
	}
}
