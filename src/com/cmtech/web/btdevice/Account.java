package com.cmtech.web.btdevice;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;
import static com.cmtech.web.MyConstant.*;
import com.cmtech.web.util.Base64;

public class Account implements IDbOperation, IJsonable {
	private int id = INVALID_ID;
	private String ver = DEFAULT_VER;
	private String userName;
	private String password;
	private String nickName;
	private String note;
	private byte[] iconData;
	
	public Account(int id) {
		this.id = id;
	}
	
	private Account(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	
	public static int login(String userName, String password) {
		return getIdFromDb(userName, password);
	}
	
	public static boolean signUp(String userName, String password) {
		if(Account.exist(userName)) return false;
		
		return new Account(userName, password).insert();
	}
	
	@Override
	public void fromJson(JSONObject json) {
		ver = json.getString("ver");
		userName = json.getString("userName");
		password = json.getString("password");
		nickName = json.getString("nickName");
		note = json.getString("note");
		String iconStr = json.getString("iconStr");
		iconData = Base64.decode(iconStr, Base64.DEFAULT);
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("ver", ver);
		json.put("userName", userName);
		json.put("password", password);
		json.put("nickName", nickName);
		json.put("note", note);
		if(iconData == null)
			json.put("iconStr", "");
		else
			json.put("iconStr", Base64.encodeToString(iconData, Base64.DEFAULT));
	
		return json;
	}
	
	@Override
	public int getId() {
		return id;		
	}
	
	@Override
	public boolean retrieve() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ver, userName, password, nickName, note, icon from Account where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				getFromResultSet(rs);
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;		
	}
	
	private void getFromResultSet(ResultSet rs) throws SQLException {
		ver = rs.getString("ver");
		userName = rs.getString("userName");
		password = rs.getString("password");
		nickName = rs.getString("nickName");
		note = rs.getString("note");
		Blob b = rs.getBlob("icon");
		if(b == null || b.length() < 1) 
			iconData = null;
		else
			iconData = b.getBytes(1, (int)b.length());
	}

	@Override
	public boolean insert() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into Account (ver, userName, password, nickName, note, icon) values (?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			int index = 1;
			ps.setString(index++, "ver");
			ps.setString(index++, userName);
			ps.setString(index++, password);
			ps.setString(index++, nickName);
			ps.setString(index++, note);
			Blob b = conn.createBlob();
			b.setBytes(1, iconData);
			ps.setBlob(index++, b);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	@Override
	public boolean update() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update Account set nickName=?, note=?, icon=? where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, nickName);
			ps.setString(2, note);
			Blob b = conn.createBlob();
			b.setBytes(1, iconData);
			ps.setBlob(3, b);
			ps.setInt(4, id);
			if(ps.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	@Override
	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean retrieveBasicInfo() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString() {
		return "userName="+userName+",password="+password+",nickName="+nickName+",note="+note+",iconDataLength="+iconData.length;
	}
	
	public static boolean exist(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select 1 from Account where id = ? limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;		
	}
	
	private static boolean exist(String userName) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select 1 from Account where userName = ? limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, userName);
			rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;		
	}
	
	private static int getIdFromDb(String userName, String password) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from Account where userName = ? and password = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, userName);
			ps.setString(2, password);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return INVALID_ID;		
	}
}
