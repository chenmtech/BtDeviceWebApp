package com.cmtech.web.dbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.btdevice.RecordType;

public class ThermoRecordDbUtil {
	public static JSONObject downloadBasicInfo(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ver, creatorPlat, creatorId, createTime, devAddress, note from thermorecord where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				String ver = rs.getString("ver");
				String creatorPlat = rs.getString("creatorPlat");
				String creatorId = rs.getString("creatorId");
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				String note = rs.getString("note");
				JSONObject json = new JSONObject();
				json.put("recordTypeCode", RecordType.THERMO.getCode());
				json.put("ver", ver);
				json.put("creatorPlat", creatorPlat);
				json.put("creatorId", creatorId);
				json.put("createTime", createTime);
				json.put("devAddress", devAddress);
				json.put("note", note);
			
				return json;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}	
}
