package com.cmtech.web.btdevice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.util.DbUtil;

public class AppUpdateInfo implements IJsonable{
	private int verCode;
	private String verName;
	private String note;
	private String url;
    private double size; // unit: MB
	
	public AppUpdateInfo() {
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
		json.put("size", size);
		
		return json;
	}

	public boolean retrieve() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select verCode, verName, note, url, size from AppUpdateInfo order by verCode desc limit 1";
		try {
			ps = conn.prepareStatement(sql);
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
		size = rs.getDouble("size");
	}

}
