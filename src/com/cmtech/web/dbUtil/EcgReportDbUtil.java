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
	
	public static JSONObject requestReport(long recordCreateTime, String recordDevAddress, long createTime, String content) {
		int recordId = RecordDbUtil.query(RecordType.ECG, recordCreateTime, recordDevAddress);
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
		
		
		
		
		return null;
	}
	
	private static int getId(long createTime, String devAddress) {
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		String tableName = "ecgreport";
		
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
			ps.setInt(5, isWaiting ? 1 : 0);
			
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
