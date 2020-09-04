package com.cmtech.web.dbUtil;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

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
	public static JSONArray downloadBasicInfo(RecordType type, String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		if(num <= 0) return null;
		
		List<RecordType> types = new ArrayList<>();
		if(type == RecordType.ALL) {
			for(RecordType t : RecordType.values()) {
				if(t != RecordType.ALL && t != RecordType.TH) {
					types.add(t);
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
			for(RecordType typeEle : types) {
				String tableName = getTableName(typeEle);
				String sql = "select id, createTime from " + tableName;
				String where = " where ";
				if(!"".equals(creatorPlat)) {
					where += "creatorPlat = ? and ";
				}
				if(!"".equals(creatorId)) {
					where += "creatorId = ? and "; 
				}
				if(!"".equals(noteSearchStr)) {
					where += "note REGEXP ? and ";
				}
				where += "createTime < ? order by createTime desc limit ?";
				sql += where;
				
				ps = conn.prepareStatement(sql);
				int i = 1;
				if(!"".equals(creatorPlat)) {
					ps.setString(i++, creatorPlat);
				}
				if(!"".equals(creatorId)) {
					ps.setString(i++, creatorId);
				}
				if(!"".equals(noteSearchStr)) {
					ps.setString(i++, noteSearchStr);
				}
				ps.setLong(i++, fromTime);
				ps.setInt(i++, num);
				rs = ps.executeQuery();
				int id = INVALID_ID;
				long createTime = -1;
				while(rs.next()) {
					id = rs.getInt("id");
					createTime = rs.getLong("createTime");
					findRecords.add(new TmpRecord(id, createTime, typeEle));
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
