package com.cmtech.web.btdevice;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

/**
 * 心电记录类
 * @author gdmc
 *
 */
public class BleEcgRecord extends BasicRecord implements IDiagnosable{
	// 心电记录中要进行数据库读写的属性字段名数组
	private static final String[] PROPERTIES = {"sampleRate", "caliValue", "leadTypeCode", "ecgData", "aveHr"};
    
	// 信号采样率
	private int sampleRate; // sample rate
	
	// 信号标定值
    private int caliValue; // calibration value of 1mV
    
    // 导联类型
    private int leadTypeCode; // lead type code
    
    // 心电数据字符串
    private String ecgData; // ecg data    
    
    // 平均心率：次/分钟
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
	
	// 获取该记录中包含的要进行数据库操作的属性字段名数组，不包括BasicRecord中的字段
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
	public JSONObject retrieveDiagnoseReport() {
		JSONObject reportJson = new JSONObject();
		reportJson.put("reportVer", getReportVer());
		reportJson.put("reportProvider", getReportProvider());
		reportJson.put("reportTime", getReportTime());
		reportJson.put("reportContent", getReportContent());
		reportJson.put("reportStatus", getReportStatus());
		reportJson.put("aveHr", aveHr);
		return reportJson;		
	}
	
	@Override
	public boolean applyForDiagnose() {
		boolean rlt = updateReportStatus(RecordType.ECG, REPORT_STATUS_DONE, REPORT_STATUS_PROCESS);
		if(rlt) {
			setReportStatus(REPORT_STATUS_PROCESS);
		}
		return rlt;
	}
	
	@Override
	public boolean updateDiagnoseReport(String reportVer, String reportProvider, long reportTime, String reportContent) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgRecord set reportVer = ?, reportProvider = ?, reportTime = ?, reportContent = ?, reportStatus = ? " 
				+ "where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			int begin = 1;
			ps.setString(begin++, reportVer);
			ps.setString(begin++, reportProvider);
			ps.setLong(begin++, reportTime);
			ps.setString(begin++, reportContent);
			ps.setInt(begin++, REPORT_STATUS_DONE);
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
	
	
	/**
	 * 获取第一个需要诊断的心电记录
	 * 是否需要诊断，依据记录的报告状态reportStatus是否为DONE，以及reportVer是否比newReportVer小
	 * 排序按照ID，最小的ID最先诊断
	 * @param newReportVer：心电诊断报告版本
	 * @return
	 */
	public static BleEcgRecord getFirstNeedDiagnoseRecord(String newReportVer) {
		String tableName = RecordType.ECG.getTableName();
		
		Connection conn = DbUtil.connect();
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select createTime, devAddress from " + tableName + 
				" where reportStatus = ? and reportVer < ? order by id limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, REPORT_STATUS_DONE);
			ps.setString(2, newReportVer);
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
