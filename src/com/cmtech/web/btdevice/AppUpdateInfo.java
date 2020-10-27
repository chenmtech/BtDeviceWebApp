package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class AppUpdateInfo implements IDbOperation, IJsonable{
	private int id = INVALID_ID;
	private int verCode;
	private String verName;
	private String note;
	private String url;
	
	public AppUpdateInfo() {
		id = getId();
	}

	@Override
	public void fromJson(JSONObject json) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("verCode", verCode);
		json.put("verName", verName);
		json.put("note", note);
		json.put("url", url);
		
		return json;
	}

	@Override
	public int getId() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return INVALID_ID;		
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		int lastId = INVALID_ID;
		try {
			String sql = "select id from AppUpdateInfo order by verCode desc limit 1";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()) {
				lastId = rs.getInt("id");
			}
			return lastId;
		} catch (SQLException e) {
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
		String sql = "select verCode, verName, note, url from AppUpdateInfo where id = ?";
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
		verCode = rs.getInt("verCode");
		verName = rs.getString("verName");
		note = rs.getString("note");
		url = rs.getString("url");
	}

	@Override
	public boolean insert() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retrieveBasicInfo() {
		// TODO Auto-generated method stub
		return false;
	}

}
