package com.cmtech.web.btdevice;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEcgReport10 {
    private static final int DONE = 0;
    private static final int WAIT_PROCESS = 1;
    private static final int PROCESSING = 2;
    
	private String ver = "1.0";
    private long reportTime = -1;
    private String content = "";
    private int status = DONE;

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
	
	public static int getId(int recordId) {
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from ecgreport where recordId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, recordId);
			rs = ps.executeQuery();
			if(rs.next()) {
				id = rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return id;		
	}
}
