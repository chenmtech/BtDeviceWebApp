package com.cmtech.web.btdevice;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEcgReport10 {
    public static final int DONE = 0;
    public static final int REQUEST = 1;
    public static final int PROCESS = 2;
    
    private int reportId = -1;
	private String ver = "1.0";
    private long reportTime = -1;
    private String content = "";
    private int status = DONE;
    private int recordId = -1;

	public BleEcgReport10() {
		
	}

	public long getReportTime() {
		return reportTime;
	}

	public void setReportTime(long reportTime) {
		this.reportTime = reportTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}	
	
	public String getVer() {
		return ver;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}	
	
	public int getReportId() {
		return reportId;
	}

	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	
	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("reportVer", ver);
		json.put("reportTime", reportTime);
		json.put("content", content);
		json.put("status", status);
		return json;
	}
	
	public boolean initAsLastRequest() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select * from ecgreport where status = ? order by reportId limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, REQUEST);
			rs = ps.executeQuery();
			if(rs.next()) {
				reportId = rs.getInt("reportId");
				ver = rs.getString("reportVer");
				reportTime = rs.getLong("reportTime");
				content = rs.getString("content");
				status = rs.getInt("status");
				recordId = rs.getInt("recordId");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;		
	}
}
