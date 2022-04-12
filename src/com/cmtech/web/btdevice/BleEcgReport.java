package com.cmtech.web.btdevice;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

import static com.cmtech.web.MyConstant.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BleEcgReport implements IDbOperation, IJsonable{
    public static final int DONE = 0;
    public static final int REQUEST = 1;
    public static final int PROCESS = 2;
    
	private String ver = DEFAULT_VER;
    private long reportTime = INVALID_TIME;
    private String content = "";
    private int status = DONE;
    private long createTime = INVALID_TIME;
    private String devAddress = "";
	
	public BleEcgReport(long createTime, String devAddress) {
		this.createTime = createTime;
		this.devAddress = devAddress;
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

	public long getCreateTime() {
		return createTime;
	}

	public String getDevAddress() {
		return devAddress;
	}

	@Override
	public void fromJson(JSONObject json) {
		if(json.has("ver")) {
			ver = json.getString("ver");			
    	} else {
    		ver = DEFAULT_VER;
    	}
		reportTime = json.getLong("reportTime");
		content = json.getString("content");
		status = json.getInt("status");
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("ver", ver);
		json.put("reportTime", reportTime);
		json.put("content", content);
		json.put("status", status);
		return json;
	}

	@Override
	public int getId() {
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String selectSql = "select id from EcgReport where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, createTime);
			ps.setString(2, devAddress);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt("id");
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
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String selectSql = "select ver, reportTime, content, status from EcgReport where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, createTime);
			ps.setString(2, devAddress);
			rs = ps.executeQuery();
			if(rs.next()) {
				getFromResultSet(rs);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;
	}

	private void getFromResultSet(ResultSet rs) throws SQLException {
		ver = rs.getString("ver");
		reportTime = rs.getLong("reportTime");
		content = rs.getString("content");
		status = rs.getInt("status");
	}

	@Override
	public boolean insert() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String insertSql = "insert into EcgReport (status, createTime, devAddress) values (?, ?, ?)";
		try {
			ps = conn.prepareStatement(insertSql);
			ps.setInt(1, status);
			ps.setLong(2, createTime);
			ps.setString(3, devAddress);
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
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "delete from EcgReport where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, createTime);
			ps.setString(2, devAddress);
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
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport set reportTime = ?, content = ?, status = ? " 
				+ "where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, reportTime);
			ps.setString(2, content);
			ps.setInt(3, status);
			ps.setLong(4, createTime);
			ps.setString(5, devAddress);
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

	public boolean updateIfBeing(int beforeStatus) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport set reportTime = ?, content = ?, status = ? " 
				+ "where createTime = ? and devAddress = ? and status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, reportTime);
			ps.setString(2, content);
			ps.setInt(3, status);
			ps.setLong(4, createTime);
			ps.setString(5, devAddress);
			ps.setInt(6, beforeStatus);
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
	
	public boolean updateStatusIfBeing(int beforeStatus) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport set status = ? where createTime = ? and devAddress = ? and status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, status);
			ps.setLong(2, createTime);
			ps.setString(3, devAddress);
			ps.setInt(4, beforeStatus);
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
	
	public static BleEcgReport getFirstRequestReport() {
		Connection conn = DbUtil.connect();
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select createTime, devAddress from EcgReport where status = ? order by id limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, BleEcgReport.REQUEST);
			rs = ps.executeQuery();
			if(rs.next()) {
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				return new BleEcgReport(createTime, devAddress);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;		
	}
}
