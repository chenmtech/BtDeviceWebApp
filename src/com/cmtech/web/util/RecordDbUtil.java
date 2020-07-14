package com.cmtech.web.util;

import static com.cmtech.web.util.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.btdevice.TmpRecord;

public class RecordDbUtil {
	// QUERY
	public static int query(RecordType type, long createTime, String devAddress) {
		return getId(type, createTime, devAddress);
	}
	
	// UPDATE NOTE
	public static boolean updateNote(RecordType type, long createTime, String devAddress, String note) {
		int id = query(type, createTime, devAddress);
		if(id == INVALID_ID) return false;
		
		return updateNote(type, id, note);
	}
	
	// DELETE
	public static boolean delete(RecordType type, long createTime, String devAddress) {
		Connection conn = DbUtil.connect();		
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
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	// UPLOAD
	public static boolean upload(RecordType type, JSONObject json) {
		switch(type) {
		case ECG:
			return EcgRecordDbUtil.upload(json);
			
		case HR:
			return HrRecordDbUtil.upload(json);
			
		case THERMO:
			return ThermoRecordDbUtil.upload(json);
			
		case EEG:
			return EegRecordDbUtil.upload(json);
			
		default:
			break;
		}
		return false;
	}
	
	// DOWNLOAD RECORD INFO
	// who: creatorPlat+creatorId
	// when: later than fromTime
	// howmuch: num
	public static JSONArray downloadBasicInfo(RecordType type, String creatorPlat, String creatorId, long fromTime, String noteFilterStr, int num) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		List<RecordType> types = new ArrayList<>();
		if(type == RecordType.ALL) {
			RecordType[] allTypes = RecordType.values();
			for(RecordType t1 : allTypes) {
				if(t1 != RecordType.ALL && t1 != RecordType.TH) {
					types.add(t1);
				}
			}
		}
		else
			types.add(type);
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		JSONArray jsonArray = new JSONArray();
		try {
			List<TmpRecord> findRecords = new ArrayList<>();
			for(RecordType t2 : types) {
				String tableName = getTableName(t2);
				String sql = "";
				if("".equals(noteFilterStr)) 
					sql = "select id, createTime from " + tableName + " where creatorPlat = ? and creatorId = ? and createTime < ? order by createTime desc limit ?";
				else
					sql = "select id, createTime from " + tableName + " where creatorPlat = ? and creatorId = ? and createTime < ? and note REGEXP ? order by createTime desc limit ?";
				
				ps = conn.prepareStatement(sql);
				ps.setString(1, creatorPlat);
				ps.setString(2, creatorId);
				ps.setLong(3, fromTime);
				if("".equals(noteFilterStr)) {
					ps.setInt(4, num);
				} else {
					ps.setString(4, noteFilterStr);
					ps.setInt(5, num);
				}
				rs = ps.executeQuery();
				int id = INVALID_ID;
				long createTime = -1;
				while(rs.next()) {
					id = rs.getInt("id");
					createTime = rs.getLong("createTime");
					findRecords.add(new TmpRecord(id, createTime, t2));
				}
			}
			Collections.sort(findRecords, new Comparator<TmpRecord>() {
                @Override
                public int compare(TmpRecord o1, TmpRecord o2) {
                	int rlt = 0;
                    if(o2.getCreateTime() > o1.getCreateTime()) rlt = 1;
                    else if(o2.getCreateTime() < o1.getCreateTime()) rlt = -1;
                    return rlt;
                }
            });

			int N = Math.min(num, findRecords.size());
			for(int i = 0; i < N; i++) {
				jsonArray.put(i, downloadBasicInfo(findRecords.get(i).getType(), findRecords.get(i).getId()));
			}
			return jsonArray;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
	
	// DOWNLOAD RECORD
	// who: creatorPlat+creatorId
	// when: later than fromTime
	// howmuch: num
	public static JSONObject download(RecordType type, long createTime, String devAddress) {
		int id = query(type, createTime, devAddress);
		if(id == INVALID_ID) return null;
		
		return download(type, id);
	}
	
	private static int getId(RecordType type, long createTime, String devAddress) {
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		String tableName = getTableName(type);
		if("".equals(tableName)) return INVALID_ID;
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from " + tableName + " where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, devAddress);
			ps.setLong(2, createTime);
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
	
	private static boolean updateNote(RecordType type, int id, String note) {
		Connection conn = DbUtil.connect();
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
			DbUtil.close(null, ps, conn);
		}
		return false;
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
		case EEG:
			return "eegrecord";
		default:
			break;
		}
		return "";
	}
	
	private static JSONObject downloadBasicInfo(RecordType type, int id) {
		switch(type) {
		case ECG:
			return EcgRecordDbUtil.downloadBasicInfo(id);
		
		case HR:
			return HrRecordDbUtil.downloadBasicInfo(id);
			
		case THERMO:
			return ThermoRecordDbUtil.downloadBasicInfo(id);
			
		case EEG:
			return EegRecordDbUtil.downloadBasicInfo(id);
			
		default:
			break;
		}
		return null;		
	}
	
	private static JSONObject download(RecordType type, int id) {
		switch(type) {
		case ECG:
			return EcgRecordDbUtil.download(id);
		
		case HR:
			return HrRecordDbUtil.download(id);
			
		case THERMO:
			return ThermoRecordDbUtil.download(id);
			
		case EEG:
			return EegRecordDbUtil.download(id);
			
		default:
			break;
		}
		return null;		
	}
}
