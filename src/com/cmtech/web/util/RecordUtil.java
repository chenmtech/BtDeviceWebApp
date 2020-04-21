package com.cmtech.web.util;

import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.btdevice.RecordType;

public class RecordUtil {
	public static int queryRecord(RecordType type, long createTime, String devAddress) {
		int id = INVALID_ID;
		
		switch(type) {
		case ECG:
			id = BleEcgRecord10.getId(createTime, devAddress);
			break;
		default:
			break;
		}
		return id;
	}
	
	public static boolean upload(BleEcgRecord10 record) {
		return record.insert();
	}
	
	public static boolean updateNote(RecordType type, long createTime, String devAddress, String note) {
		int id = queryRecord(type, createTime, devAddress);
		if(id == INVALID_ID) return false;
		
		return updateNote(id, note);
	}
	
	public static boolean deleteRecord(RecordType type, long createTime, String devAddress) {
		Connection conn = MySQLUtil.getConnection();		
		if(conn == null) return false;
		
		int id = queryRecord(type, createTime, devAddress);
		if(id == INVALID_ID) return false;
		
		PreparedStatement ps = null;
		String sql = "delete from ecgrecord where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
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
		}
		return false;
	}
	
	public static  JSONArray getRecord(RecordType type, long fromTime, String creatorPlat, String creatorId) {
		Connection conn = MySQLUtil.getConnection();		
		if(conn == null) return null;
		if(type != RecordType.ECG) return null;
		
		PreparedStatement ps = null;
		ResultSet rlt = null;
		String sql = "select id from ecgrecord where creatorPlat = ? and creatorId = ? and createTime >= ? order by createTime desc";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, creatorPlat);
			ps.setString(2, creatorId);
			ps.setLong(3, fromTime);
			rlt = ps.executeQuery();
			int id = -1;
			JSONArray jsonArray = new JSONArray();
			int i = 0;
			while(rlt.next()) {
				id = rlt.getInt("id");
				System.out.println("id=" + id);
				jsonArray.put(i++, getRecord(id));
			}
			return jsonArray;
			/*if(rlt.next()) {
				int id = rlt.getInt("id");
				return getRecord(id);
			}*/
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
		}
		return null;
	}
	
	public static JSONObject getRecord(int id) {
		Connection conn = MySQLUtil.getConnection();		
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
		}
		return null;
	}

	private static boolean updateNote(int id, String note) {
		Connection conn = MySQLUtil.getConnection();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update ecgrecord set note = ? where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, note);
			ps.setInt(2, id);
			
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
		}
		return false;
	}
}
