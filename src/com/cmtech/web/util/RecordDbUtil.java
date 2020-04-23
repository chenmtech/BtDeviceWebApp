package com.cmtech.web.util;

import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.RecordType;

public class RecordDbUtil {
	// QUERY
	public static int query(RecordType type, long createTime, String devAddress) {
		return getId(type, createTime, devAddress);
	}
	
	// UPLOAD
	public static boolean upload(RecordType type, JSONObject json) {
		switch(type) {
		case ECG:
			return EcgRecordDbUtil.upload(json);
			
		case HR:
			return HrRecordDbUtil.upload(json);
		default:
			break;
		}
		return false;
	}
	
	// UPDATE NOTE
	public static boolean updateNote(RecordType type, long createTime, String devAddress, String note) {
		int id = query(type, createTime, devAddress);
		if(id == INVALID_ID) return false;
		
		return updateNote(type, id, note);
	}
	
	// DELETE
	public static boolean delete(RecordType type, long createTime, String devAddress) {
		Connection conn = MySQLUtil.connect();		
		if(conn == null) return false;
		
		int id = query(type, createTime, devAddress);
		if(id == INVALID_ID) return false;
		
		String tableName = getTableName(type);
		if("".equals(tableName)) return false;
		
		PreparedStatement ps = null;
		String sql = "delete from " + tableName + " where id = ?";
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
			
			MySQLUtil.disconnect(conn);
		}
		return false;
	}
	
	// DOWNLOAD
	public static JSONArray download(RecordType type, long fromTime, String creatorPlat, String creatorId, int num) {
		Connection conn = MySQLUtil.connect();		
		if(conn == null) return null;
		
		String tableName = getTableName(type);
		
		PreparedStatement ps = null;
		ResultSet rlt = null;
		String sql = "select id from " + tableName + " where creatorPlat = ? and creatorId = ? and createTime < ? order by createTime desc limit ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, creatorPlat);
			ps.setString(2, creatorId);
			ps.setLong(3, fromTime);
			ps.setInt(4, num);
			rlt = ps.executeQuery();
			int id = INVALID_ID;
			JSONArray jsonArray = new JSONArray();
			int i = 0;
			while(rlt.next()) {
				id = rlt.getInt("id");
				System.out.println("id=" + id);
				jsonArray.put(i++, download(type, id));
			}
			return jsonArray;
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
	
	private static JSONObject download(RecordType type, int id) {
		switch(type) {
		case ECG:
			return EcgRecordDbUtil.download(id);
		
		case HR:
			return HrRecordDbUtil.download(id);
		default:
			break;
		}
		return null;		
	}

	private static boolean updateNote(RecordType type, int id, String note) {
		Connection conn = MySQLUtil.connect();
		if(conn == null) return false;
		
		String tableName = getTableName(type);
		
		PreparedStatement ps = null;
		String sql = "update " + tableName + " set note = ? where id = ?";
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
			
			MySQLUtil.disconnect(conn);
		}
		return false;
	}
	
	private static int getId(RecordType type, long createTime, String devAddress) {
		Connection conn = MySQLUtil.connect();
		if(conn == null) return INVALID_ID;
		
		String tableName = getTableName(type);
		if("".equals(tableName)) return INVALID_ID;
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rlt = null;
		String sql = "select id from " + tableName + " where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, devAddress);
			ps.setLong(2, createTime);
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
	
	private static String getTableName(RecordType type) {
		switch(type) {
		case ECG:
			return "ecgrecord";
		case HR:
			return "hrrecord";			
		case THERMO:
			return "thermorecord";
		case TH:
			return "threcord";
		}
		return "";
	}
}
