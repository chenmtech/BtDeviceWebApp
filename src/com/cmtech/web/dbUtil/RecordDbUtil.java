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

import com.cmtech.web.btdevice.BasicRecord;
import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.btdevice.BleEcgReport10;
import com.cmtech.web.btdevice.RecordFactory;
import com.cmtech.web.btdevice.RecordType;

public class RecordDbUtil {

    public static final int CODE_REPORT_SUCCESS = 0;
    public static final int CODE_REPORT_FAILURE = 1;
    public static final int CODE_REPORT_ADD_NEW = 2;
    public static final int CODE_REPORT_PROCESSING = 3;
    public static final int CODE_REPORT_REQUEST_AGAIN = 4;
    public static final int CODE_REPORT_NO_NEW = 5;
	
	// QUERY
	public static int getId(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return INVALID_ID;
		return record.getId();
	}
	
	// UPDATE NOTE
	public static boolean updateNote(RecordType type, long createTime, String devAddress, String note) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		record.setNote(note);
		return record.update();
	}
	
	// DELETE
	public static boolean delete(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		return record.delete();
	}
	
	// INSERT
	public static boolean insert(RecordType type, JSONObject json) {
		long createTime = json.getLong("createTime");
		String devAddress = json.getString("devAddress");
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		record.fromJson(json);
		return record.insert();
	}
	
	// DOWNLOAD RECORD
	// who: creatorPlat+creatorId
	// when: later than fromTime
	// howmuch: num
	public static JSONObject download(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return null;
		
		if(!record.retrieve()) return null;
		return record.toJson();
	}
	
	// DOWNLOAD BASIC RECORD INFO
	// who: creatorPlat+creatorId
	// when: later than fromTime
	// howmuch: num
	public static JSONArray downloadBasicInfo(RecordType type, String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num) {
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
		
		JSONArray jsonArray = new JSONArray();
		List<TmpRecord> found = new ArrayList<>();
		for(RecordType t : types) {
			found.addAll(searchRecord(t, creatorPlat, creatorId, fromTime, noteSearchStr, num));
		}
		Collections.sort(found, new Comparator<TmpRecord>() {
		    @Override
		    public int compare(TmpRecord o1, TmpRecord o2) {
		    	int rlt = 0;
		        if(o2.getCreateTime() > o1.getCreateTime()) rlt = 1;
		        else if(o2.getCreateTime() < o1.getCreateTime()) rlt = -1;
		        return rlt;
		    }
		});

		int N = Math.min(num, found.size());
		for(int i = 0; i < N; i++) {
			jsonArray.put(i, downloadBasicInfo(found.get(i).getType(), found.get(i).getId()));
		}
		
		return jsonArray;
	}
	
	public static JSONObject requestReport(long createTime, String devAddress) {
		BleEcgRecord10 record = (BleEcgRecord10)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		int reportCode = record.requestReport();
		JSONObject reportResult = new JSONObject();
		reportResult.put("reportCode", reportCode);
		return reportResult;
	}
	
	public static JSONObject downloadReport(long createTime, String devAddress) {
		BleEcgRecord10 record = (BleEcgRecord10)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		int reportCode = record.updateReportFromDb();
		JSONObject reportResult = new JSONObject();
		reportResult.put("reportCode", reportCode);
		if(reportCode == CODE_REPORT_SUCCESS)
			reportResult.put("report", record.getReport().toJson());
		return reportResult;
	}
	
	public static JSONObject downloadLastRequestRecord() {
		int ecgReportId = getLastRequestReportId();
		if(ecgReportId == INVALID_ID) return null;
		
		if(!applyForProcessReport(ecgReportId)) return null;
		
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select createTime, devAddress from EcgRecord, EcgReport where EcgReport.ecgReportId = ? and EcgRecord.id = EcgReport.recordId";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, ecgReportId);
			rs = ps.executeQuery();
			if(rs.next()) {
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				return download(RecordType.ECG, createTime, devAddress);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
	
	public static boolean updateReport(long createTime, String devAddress, long reportTime, String content) {
		BleEcgRecord10 record = (BleEcgRecord10)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		if(record == null) return false;
		
		int recordId = record.getId();
		if(recordId == INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport set reportTime = ?, content = ?, status = ? where recordId = ? and status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, reportTime);
			ps.setString(2, content);
			ps.setInt(3, BleEcgReport10.DONE);
			ps.setInt(4, recordId);
			ps.setInt(5, BleEcgReport10.PROCESS);
			if(ps.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;	
	}
	
	private static int getLastRequestReportId() {
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ecgReportId from EcgReport where status = ? order by ecgReportId limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, BleEcgReport10.REQUEST);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt("ecgReportId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return INVALID_ID;		
	}
	
	private static boolean applyForProcessReport(int reportId) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport set status = ? where ecgReportId = ? and status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, BleEcgReport10.PROCESS);
			ps.setInt(2, reportId);
			ps.setInt(3, BleEcgReport10.REQUEST);
			if(ps.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;		
	}
	
	private static JSONObject downloadBasicInfo(RecordType type, int id) {
		String tableName = getTableName(type);
		
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ver, creatorPlat, creatorId, createTime, devAddress, recordSecond, note from " + tableName + " where id = ?";
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
				json.put("recordTypeCode", type.getCode());
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
	
	private static List<TmpRecord> searchRecord(RecordType type, String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num) {
		if(num <= 0) return null;
		String tableName = getTableName(type);
		if("".equals(tableName)) return null;
		
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;		
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			List<TmpRecord> found = new ArrayList<>();
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
			while(rs.next()) {
				int id = rs.getInt("id");
				long createTime = rs.getLong("createTime");
				found.add(new TmpRecord(type, id, createTime));
			}
			
			return found;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
	
	public static String getTableName(RecordType type) {
		switch(type) {
		case ECG:
			return "EcgRecord";
		case HR:
			return "HrRecord";			
		case THERMO:
			return "ThermoRecord";
		case TH:
			return "ThRecord";
		case EEG:
			return "EegRecord";
		default:
			break;
		}
		return "";
	}
	
	private static class TmpRecord {
		int id;
		long createTime;
		RecordType type;
		
		public TmpRecord(RecordType type, int id, long createTime) {
			this.id = id;
			this.createTime= createTime;
			this.type = type;
		}

		public int getId() {
			return id;
		}

		public long getCreateTime() {
			return createTime;
		}

		public RecordType getType() {
			return type;
		}
		
		
	}
}
