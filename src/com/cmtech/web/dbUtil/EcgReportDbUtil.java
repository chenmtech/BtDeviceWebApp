package com.cmtech.web.dbUtil;

import org.json.JSONObject;
import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cmtech.web.btdevice.RecordType;

public class EcgReportDbUtil {
    public static final int CODE_REPORT_SUCCESS = 0;
    public static final int CODE_REPORT_FAILURE = 1;
    public static final int CODE_REPORT_ADD_NEW = 2;
    public static final int CODE_REPORT_PROCESSING = 3;
    public static final int CODE_REPORT_REQUEST_AGAIN = 4;
    public static final int CODE_REPORT_NO_NEW = 5;
	
	public static JSONObject requestReport(long recordCreateTime, String recordDevAddress, long createTime, String content) {
		int recordId = RecordDbUtil.getRecordId(RecordType.ECG, recordCreateTime, recordDevAddress);
		int reportCode = CODE_REPORT_FAILURE;
		JSONObject reportResult = new JSONObject();
		
		if(recordId == INVALID_ID) {
			reportCode = CODE_REPORT_FAILURE;
			reportResult.put("reportCode", reportCode);
			return reportResult;
		}
		
		int reportId = getId(recordCreateTime, recordDevAddress);
		if(reportId == INVALID_ID) {
			if(add(recordCreateTime, recordDevAddress, createTime, content, true)) {
				reportCode = CODE_REPORT_ADD_NEW;
				reportResult.put("reportCode", reportCode);
				return reportResult;
			} else {
				reportCode = CODE_REPORT_FAILURE;
				reportResult.put("reportCode", reportCode);
				return reportResult;
			}
		}
		
		JSONObject report = download(reportId);
		if(report == null) {
			reportCode = CODE_REPORT_FAILURE;
			reportResult.put("reportCode", reportCode);
			return reportResult;
		}
		
		if(report.getBoolean("isWaiting")) {
			reportCode = CODE_REPORT_PROCESSING;
			reportResult.put("reportCode", reportCode);
			return reportResult;
		} 
		
		if(report.getLong("createTime") > createTime) {
			reportCode = CODE_REPORT_SUCCESS;
			reportResult.put("reportCode", reportCode);
			reportResult.put("report", report);
			return reportResult;
		} else {
			if(update(reportId, createTime, content, true)) {
				reportCode = CODE_REPORT_REQUEST_AGAIN;
				reportResult.put("reportCode", reportCode);
				return reportResult;
			} else {
				reportCode = CODE_REPORT_FAILURE;
				reportResult.put("reportCode", reportCode);
				return reportResult;
			}
		}
	}
	
	public static JSONObject getNewReport(long recordCreateTime, String recordDevAddress, long createTime, String content) {
		int recordId = RecordDbUtil.getRecordId(RecordType.ECG, recordCreateTime, recordDevAddress);
		int reportCode = CODE_REPORT_NO_NEW;
		JSONObject reportResult = new JSONObject();
		
		if(recordId == INVALID_ID) {
			reportCode = CODE_REPORT_NO_NEW;
			reportResult.put("reportCode", reportCode);
			return reportResult;
		}
		
		int reportId = getId(recordCreateTime, recordDevAddress);
		if(reportId == INVALID_ID) {
			reportCode = CODE_REPORT_NO_NEW;
			reportResult.put("reportCode", reportCode);
			return reportResult;
		}
		
		JSONObject report = download(reportId);
		if(report == null) {
			reportCode = CODE_REPORT_FAILURE;
			reportResult.put("reportCode", reportCode);
			return reportResult;
		}
		
		if(report.getBoolean("isWaiting")) {
			reportCode = CODE_REPORT_PROCESSING;
			reportResult.put("reportCode", reportCode);
			return reportResult;
		} 
		
		if(report.getLong("createTime") > createTime) {
			reportCode = CODE_REPORT_SUCCESS;
			reportResult.put("reportCode", reportCode);
			reportResult.put("report", report);
			return reportResult;
		} else {
			reportCode = CODE_REPORT_NO_NEW;
			reportResult.put("reportCode", reportCode);
			return reportResult;
		}
	}
	
	public static JSONObject download(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ver, recordCreateTime, recordDevAddress, createTime, content, isWaiting from ecgreport where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				String ver = rs.getString("ver");
				long recordCreateTime = rs.getLong("recordCreateTime");
				String recordDevAddress = rs.getString("recordDevAddress");
				long createTime = rs.getLong("createTime");
				String content = rs.getString("content");
				int isWaiting = rs.getInt("isWaiting");
				JSONObject json = new JSONObject();
				json.put("ver", ver);
				json.put("createTime", createTime);
				json.put("content", content);
				json.put("isWaiting", (isWaiting == 0) ? false : true);
			
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
	
	private static boolean update(int id, long createTime, String content, boolean isWaiting) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update ecgreport" + " set createTime = ? , content = ? , isWaiting = ? where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, createTime);
			ps.setString(2,  content);
			ps.setBoolean(3, isWaiting);
			ps.setInt(4, id);
			
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
	
	private static int getId(long recordCreateTime, String recordDevAddress) {
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		String tableName = "ecgreport";
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from " + tableName + " where recordDevAddress = ? and recordCreateTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, recordDevAddress);
			ps.setLong(2, recordCreateTime);
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
	
	private static boolean add(long recordCreateTime, String recordDevAddress, long createTime, String content, boolean isWaiting) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into ecgreport (recordCreateTime, recordDevAddress, createTime, content, isWaiting) "
				+ "values (?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, recordCreateTime);
			ps.setString(2, recordDevAddress);
			ps.setLong(3, createTime);
			ps.setString(4, content);
			ps.setBoolean(5, isWaiting);
			
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
}
