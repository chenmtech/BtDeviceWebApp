package com.cmtech.web.btdevice;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BleEcgReport10 implements IDbOperation{
    public static final int DONE = 0;
    public static final int REQUEST = 1;
    public static final int PROCESS = 2;
    
    private int reportId = INVALID_ID;
	private String ver = "1.0";
    private long reportTime = -1;
    private String content = "";
    private int status = DONE;
    private int recordId = INVALID_ID;

	public BleEcgReport10() {
		
	}
	
	public BleEcgReport10(int recordId) {
		this.recordId = recordId;
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

	@Override
	public int getId() {
		if(reportId != INVALID_ID) return reportId;
		if(recordId == INVALID_ID) return INVALID_ID;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String selectSql = "select ecgReportId from EcgReport where recordId = ?";
		try {
			ps = conn.prepareStatement(selectSql);
			ps.setInt(1, recordId);
			rs = ps.executeQuery();
			if(rs.next()) {
				reportId = rs.getInt("ecgReportId");
				return reportId;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return INVALID_ID;
	}

	@Override
	public boolean retrieve() {
		if(recordId == INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String selectSql = "select ecgReportId, reportVer, reportTime, content, status from EcgReport where recordId = ?";
		try {
			ps = conn.prepareStatement(selectSql);
			ps.setInt(1, recordId);
			rs = ps.executeQuery();
			if(rs.next()) {
				reportId = rs.getInt("ecgReportId");
				ver = rs.getString("reportVer");
				reportTime = rs.getLong("reportTime");
				content = rs.getString("content");
				status = rs.getInt("status");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;
	}

	@Override
	public boolean insert() {
		if(recordId == INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String insertSql = "insert into EcgReport (status, recordId) values (?, ?)";
		try {
			ps = conn.prepareStatement(insertSql);
			ps.setInt(1, status);
			ps.setInt(2, recordId);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;
	}

	@Override
	public boolean delete() {
		if(recordId == INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "delete from EcgReport where recordId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, recordId);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}

	@Override
	public boolean update() {
		if(recordId == INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport set reportTime = ?, content = ?, status = ? " 
				+ "where recordId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, reportTime);
			ps.setString(2, content);
			ps.setInt(3, status);
			ps.setInt(4, recordId);
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
	
	public boolean updateStatus(int beforeStatus, int afterStatus) {
		if(recordId == INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport set status = ? where recordId = ? and status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, afterStatus);
			ps.setInt(2, recordId);
			ps.setInt(3, beforeStatus);
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
}
