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

import com.cmtech.web.btdevice.AbstractRecord;
import com.cmtech.web.btdevice.RecordFactory;
import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.btdevice.TmpRecord;

public class RecordDbUtil {
	// QUERY
	public static int getId(RecordType type, long createTime, String devAddress) {
		AbstractRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return INVALID_ID;
		return record.retrieveId();
	}
	
	// UPDATE NOTE
	public static boolean updateNote(RecordType type, long createTime, String devAddress, String note) {
		AbstractRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		record.setNote(note);
		return record.updateNote();
	}
	
	// DELETE
	public static boolean delete(RecordType type, long createTime, String devAddress) {
		AbstractRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		return record.delete();
	}
	
	// INSERT
	public static boolean insert(RecordType type, JSONObject json) {
		AbstractRecord record = RecordFactory.createFromJson(type, json);
		if(record == null) return false;
		return record.insert();
	}
	
	// DOWNLOAD RECORD
	// who: creatorPlat+creatorId
	// when: later than fromTime
	// howmuch: num
	public static JSONObject download(RecordType type, long createTime, String devAddress) {
		AbstractRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return null;
		
		if(!record.retrieve()) return null;
		return record.packToJson();
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
}
