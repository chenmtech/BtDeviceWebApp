package com.cmtech.web.dbUtil;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.btdevice.BleHrRecord10;
import com.cmtech.web.btdevice.RecordType;

public class HrRecordDbUtil {
	public static boolean upload(JSONObject json) {
		BleHrRecord10 record = BleHrRecord10.createFromJson(json);
		if(record == null) return false;
		
		int id = RecordDbUtil.query(RecordType.HR, record.getCreateTime(), record.getDevAddress());
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into hrrecord (ver, createTime, devAddress, creatorPlat, creatorId, note, hrList, hrMax, hrAve, hrHist, recordSecond) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, record.getVer());
			ps.setLong(2, record.getCreateTime());
			ps.setString(3, record.getDevAddress());
			ps.setString(4, record.getCreatorPlat());
			ps.setString(5, record.getCreatorPlatId());
			ps.setString(6, record.getNote());
			ps.setString(7, record.getHrList());
			ps.setShort(8, record.getHrMax());
			ps.setShort(9, record.getHrAve());
			ps.setString(10, record.getHrHist());
			ps.setInt(11, record.getRecordSecond());
			
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

	public static JSONObject downloadBasicInfo(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ver, creatorPlat, creatorId, createTime, devAddress, recordSecond, note from hrrecord where id = ?";
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
				int recordSecond = rs.getInt("recordSecond");
				String note = rs.getString("note");
				JSONObject json = new JSONObject();
				json.put("recordTypeCode", RecordType.HR.getCode());
				json.put("ver", ver);
				json.put("creatorPlat", creatorPlat);
				json.put("creatorId", creatorId);
				json.put("createTime", createTime);
				json.put("devAddress", devAddress);
				json.put("recordSecond", recordSecond);
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
	
	public static JSONObject download(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select createTime, devAddress, creatorPlat, creatorId, note, hrList, hrMax, hrAve, hrHist, recordSecond from hrrecord where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				String creatorPlat = rs.getString("creatorPlat");
				String creatorId = rs.getString("creatorId");
				String note = rs.getString("note");
				String hrList = rs.getString("hrList");
				short hrMax = rs.getShort("hrMax");
				short hrAve = rs.getShort("hrAve");
				String hrHist = rs.getString("hrHist");
				int recordSecond = rs.getInt("recordSecond");
				JSONObject json = new JSONObject();
				json.put("recordTypeCode", RecordType.HR.getCode());
				json.put("createTime", createTime);
				json.put("devAddress", devAddress);
				json.put("creatorPlat", creatorPlat);
				json.put("creatorId", creatorId);
				json.put("note", note);
				json.put("hrList", hrList);
				json.put("hrMax", hrMax);
				json.put("hrAve", hrAve);
				json.put("hrHist", hrHist);
				json.put("recordSecond", recordSecond);
			
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
