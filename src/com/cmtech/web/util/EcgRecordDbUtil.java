package com.cmtech.web.util;

import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.btdevice.RecordType;

public class EcgRecordDbUtil {
	
	public static boolean upload(JSONObject json) {
		BleEcgRecord10 record = parseJson(json);
		
		int id = RecordDbUtil.query(RecordType.ECG, record.getCreateTime(), record.getDevAddress());
		if(id != INVALID_ID) return false;
		
		Connection conn = MySQLUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into ecgrecord (ver, createTime, devAddress, creatorPlat, creatorId, sampleRate, caliValue, leadTypeCode, recordSecond, note, ecgData) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, record.getVer());
			ps.setLong(2, record.getCreateTime());
			ps.setString(3, record.getDevAddress());
			ps.setString(4, record.getCreatorPlat());
			ps.setString(5, record.getCreatorPlatId());
			ps.setInt(6, record.getSampleRate());
			ps.setInt(7, record.getCaliValue());
			ps.setInt(8, record.getLeadTypeCode());
			ps.setInt(9, record.getRecordSecond());
			ps.setString(10, record.getNote());
			ps.setString(11, record.getEcgData());
			
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
	
	public static JSONObject download(int id) {
		Connection conn = MySQLUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rlt = null;
		String sql = "select createTime, devAddress, creatorPlat, creatorId, sampleRate, caliValue, leadTypeCode, recordSecond, note, ecgData from ecgrecord where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rlt = ps.executeQuery();
			if(rlt.next()) {
				long createTime = rlt.getLong("createTime");
				String devAddress = rlt.getString("devAddress");
				String creatorPlat = rlt.getString("creatorPlat");
				String creatorId = rlt.getString("creatorId");
				int sampleRate = rlt.getInt("sampleRate");
				int caliValue = rlt.getInt("caliValue");
				int leadTypeCode = rlt.getInt("leadTypeCode");
				int recordSecond = rlt.getInt("recordSecond");
				String note = rlt.getString("note");
				String ecgData = rlt.getString("ecgData");
				JSONObject json = new JSONObject();
				json.put("createTime", createTime);
				json.put("devAddress", devAddress);
				json.put("creatorPlat", creatorPlat);
				json.put("creatorId", creatorId);
				json.put("sampleRate", sampleRate);
				json.put("caliValue", caliValue);
				json.put("leadTypeCode", leadTypeCode);
				json.put("recordSecond", recordSecond);
				json.put("note", note);
				json.put("ecgData", ecgData);
			
				return json;
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
		return null;
	}
	
	private static BleEcgRecord10 parseJson(JSONObject jsonObject) {
		String ver = jsonObject.getString("ver");
		long createTime = jsonObject.getLong("createTime");
		String devAddress = jsonObject.getString("devAddress");
		String creatorPlat = jsonObject.getString("creatorPlat");
		String creatorId = jsonObject.getString("creatorId");
		int sampleRate = jsonObject.getInt("sampleRate");
		int caliValue = jsonObject.getInt("caliValue");
		int leadTypeCode = jsonObject.getInt("leadTypeCode");
		int recordSecond = jsonObject.getInt("recordSecond");
		String note = jsonObject.getString("note");
		String ecgData = jsonObject.getString("ecgData");

		BleEcgRecord10 record = new BleEcgRecord10();
		record.setVer(ver);
		record.setCreateTime(createTime);
		record.setDevAddress(devAddress);
		record.setCreator(new Account(creatorPlat, creatorId));
		record.setSampleRate(sampleRate);
		record.setCaliValue(caliValue);
		record.setLeadTypeCode(leadTypeCode);
		record.setRecordSecond(recordSecond);
		record.setNote(note);
		record.setEcgData(ecgData);
		return record;
	}
}
