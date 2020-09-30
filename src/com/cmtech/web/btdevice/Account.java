package com.cmtech.web.btdevice;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;
import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;
import com.cmtech.web.util.Base64;

public class Account implements IDbOperation, IJsonable {
	private String platName;
	private String platId;
	private String name;
	private String note;
	private byte[] iconData;
	
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
	
	@Override
	public void fromJson(JSONObject json) {
		name = json.getString("name");
		note = json.getString("note");
		String iconStr = json.getString("iconStr");
		iconData = Base64.decode(iconStr, Base64.DEFAULT);
	}

	@Override
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
	public int getId() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from Account where platName = ? and platId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, platName);
			ps.setString(2, platId);
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
	
	@Override
	public boolean retrieve() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select name, note, icon from Account where platName = ? and platId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, platName);
			ps.setString(2, platId);
			rs = ps.executeQuery();
			if(rs.next()) {
				name = rs.getString("name");
				note = rs.getString("note");
				Blob b = rs.getBlob("icon");
				if(b == null || b.length() < 1) 
					iconData = null;
				else
					iconData = b.getBytes(1, (int)b.length());
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
	
	@Override
	public boolean insert() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into Account (platName, platId, name, note, icon) values (?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, platName);
			ps.setString(2, platId);
			ps.setString(3, name);
			ps.setString(4, note);
			Blob b = conn.createBlob();
			b.setBytes(1, iconData);
			ps.setBlob(5, b);
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
		String sql = "update Account set name=?, note=?, icon=? where platName = ? and platId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, note);
			Blob b = conn.createBlob();
			b.setBytes(1, iconData);
			ps.setBlob(3, b);
			ps.setString(4, platName);
			ps.setString(5, platId);
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
	public String toString() {
		return "platName="+platName+",platId="+platId+",name="+name+",note="+note+",iconDataLength="+iconData.length;
	}
	
	public boolean login() {
		return (getId() != INVALID_ID);
	}
	
	public boolean signUp() {
		return (getId() == INVALID_ID && insert());
	}
}
