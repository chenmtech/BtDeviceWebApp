package com.cmtech.web.btdevice;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEcgRecord extends BasicRecord implements IDiagnosable{
	private static final String[] PROPERTIES = {"sampleRate", "caliValue", "leadTypeCode", "ecgData", "aveHr"};
    
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private String ecgData; // ecg data
    
    private int aveHr = 0; // average hr


    public BleEcgRecord(long createTime, String devAddress) {
    	super(RecordType.ECG, createTime, devAddress);
    }
    
    public int getSampleRate() {
		return sampleRate;
	}

	public int getCaliValue() {
		return caliValue;
	}

	public int getLeadTypeCode() {
		return leadTypeCode;
	}

	public String getEcgData() {
		return ecgData;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setCaliValue(int caliValue) {
		this.caliValue = caliValue;
	}

	public void setLeadTypeCode(int leadTypeCode) {
		this.leadTypeCode = leadTypeCode;
	}

	public void setEcgData(String ecgData) {
		this.ecgData = ecgData;
	}
	
	@Override
    public String[] getProperties() {    	
    	return PROPERTIES;
    }
	
    @Override
	public void fromJson(JSONObject json) {
		super.fromJson(json);		
		sampleRate = json.getInt("sampleRate");
		caliValue = json.getInt("caliValue");
		leadTypeCode = json.getInt("leadTypeCode");
		ecgData = json.getString("ecgData");
		aveHr = json.getInt("aveHr");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("caliValue", caliValue);
		json.put("leadTypeCode", leadTypeCode);
		json.put("ecgData", ecgData);
		json.put("aveHr", aveHr);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		caliValue = rs.getInt("caliValue");
		leadTypeCode = rs.getInt("leadTypeCode");
		ecgData = rs.getString("ecgData");
		aveHr = rs.getInt("aveHr");
	}
	
	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, caliValue);
		ps.setInt(begin++, leadTypeCode);
		ps.setString(begin++, ecgData);
		ps.setInt(begin++, aveHr);
		return begin;
	}
	
	@Override
	public JSONObject retrieveDiagnose() {
		boolean rlt = true;
		
		int status = getReportStatus();
		switch(status) {
			case STATUS_DONE:
				rlt = updateStatus(status, STATUS_REQUEST);
				break;				
				
			case STATUS_REQUEST:
			case STATUS_PROCESS:
				break;
				
			case STATUS_WAIT_READ:
				rlt = updateStatus(status, STATUS_DONE);
				break;
				
				default:
					rlt = false;
					break;
		}		
		
		if(rlt)
			return getReportJson();
		else
			return null;
	}
	
	@Override
	public boolean applyForDiagnose() {
		boolean rlt = updateStatus(STATUS_REQUEST, STATUS_PROCESS);
		if(rlt) {
			setReportStatus(STATUS_PROCESS);
		}
		return rlt;
	}
	
	@Override
	public boolean updateDiagnose(long reportTime, String reportContent) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgRecord set reportTime = ?, reportContent = ?, reportStatus = ? " 
				+ "where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			int begin = 1;
			ps.setLong(begin++, reportTime);
			ps.setString(begin++, reportContent);
			ps.setInt(begin++, STATUS_WAIT_READ);
			ps.setLong(begin++, getCreateTime());
			ps.setString(begin++, getDevAddress());
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
	
	public JSONObject getReportJson() {
		JSONObject reportJson = new JSONObject();
		reportJson.put("reportVer", getReportVer());
		reportJson.put("reportClient", getReportClient());
		reportJson.put("reportTime", getReportTime());
		reportJson.put("reportContent", getReportContent());
		reportJson.put("reportStatus", getReportStatus());
		reportJson.put("aveHr", aveHr);
		return reportJson;
	}
	
	private boolean updateStatus(int fromStatus, int toStatus) {
		String tableName = RecordType.ECG.getTableName();
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update " + tableName +" set reportStatus = ?, reportClient = ? where createTime = ? and devAddress = ? and reportStatus = ?";
		try {
			int begin = 1;
			ps = conn.prepareStatement(sql);
			ps.setInt(begin++, toStatus);
			ps.setInt(begin++, REPORT_CLIENT_REMOTE);
			ps.setLong(begin++, getCreateTime());
			ps.setString(begin++, getDevAddress());
			ps.setInt(begin++, fromStatus);
			if(ps.executeUpdate() != 0) {
				setReportStatus(toStatus);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;	
	}
	
	public static BleEcgRecord getFirstRequestRecord() {
		String tableName = RecordType.ECG.getTableName();
		
		Connection conn = DbUtil.connect();
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select createTime, devAddress from " + tableName + " where reportStatus = ? order by id limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, STATUS_REQUEST);
			rs = ps.executeQuery();
			if(rs.next()) {
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				return new BleEcgRecord(createTime, devAddress);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
}
