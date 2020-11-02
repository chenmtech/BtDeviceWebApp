package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.INVALID_TIME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEcgRecord extends BasicRecord implements IDiagnosable{
	private static final String[] PROPERTIES = {"sampleRate", "caliValue", "leadTypeCode", "ecgData", "reportVer", "reportTime", "content", "status", "aveHr"};
	private static final String DEFAULT_REPORT_VER = "0.0";
	
	public static final int STATUS_DONE = 0;
    public static final int STATUS_REQUEST = 1;
    public static final int STATUS_PROCESS = 2;
    
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private String ecgData; // ecg data
    
    private String reportVer = DEFAULT_REPORT_VER;
    private long reportTime = INVALID_TIME; // diagnose report time
    private String content = ""; // diagnose result
    private int status = STATUS_DONE; // diagnose status
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
		if(json.has("report")) {
			JSONObject reportJson = json.getJSONObject("report");
			reportVer = reportJson.getString("reportVer");
			reportTime = reportJson.getLong("reportTime");
			content = reportJson.getString("content");
			status = reportJson.getInt("status");
			if(reportJson.has("aveHr"))
				aveHr = reportJson.getInt("aveHr");
		}
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("caliValue", caliValue);
		json.put("leadTypeCode", leadTypeCode);
		json.put("ecgData", ecgData);
		JSONObject reportJson = new JSONObject();
		reportJson.put("reportVer", reportVer);
		reportJson.put("reportTime", reportTime);
		reportJson.put("content", content);
		reportJson.put("status", status);
		reportJson.put("aveHr", aveHr);
		json.put("report", reportJson);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		caliValue = rs.getInt("caliValue");
		leadTypeCode = rs.getInt("leadTypeCode");
		ecgData = rs.getString("ecgData");
		reportVer = rs.getString("reportVer");
		reportTime = rs.getLong("reportTime");
		content = rs.getString("content");
		status = rs.getInt("status");
		aveHr = rs.getInt("aveHr");
	}
	
	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, caliValue);
		ps.setInt(begin++, leadTypeCode);
		ps.setString(begin++, ecgData);
		ps.setString(begin++, reportVer);
		ps.setLong(begin++, reportTime);
		ps.setString(begin++, content);
		ps.setInt(begin++, status);
		ps.setInt(begin++, aveHr);
		return begin;
	}
	
	// UPDATE
    @Override
	public boolean update() {
    	String tableName = RecordType.ECG.getTableName();
    	
    	Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update " + tableName + " set reportVer= ?, reportTime=?, content=?, status=?, aveHr = ?, note = ? where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			int begin = 1;
			ps.setString(begin++, reportVer);
			ps.setLong(begin++, reportTime);
			ps.setString(begin++, content);
			ps.setInt(begin++, status);
			ps.setInt(begin++, aveHr);
			ps.setString(begin++, getNote());
			ps.setLong(begin++, getCreateTime());
			ps.setString(begin++, getDevAddress());
			if(ps.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	@Override
	public int requestDiagnose() {
		/*if(!report.retrieve()) {
			report.setStatus(BleEcgReport10.REQUEST);
			if(report.insert())
				return IDiagnosable.CODE_REPORT_ADD_NEW;
			else
				return IDiagnosable.CODE_REPORT_FAILURE;
		} else {
			report.setStatus(BleEcgReport10.REQUEST);
			if(report.updateStatusIfBeing(BleEcgReport10.DONE)) {
				return IDiagnosable.CODE_REPORT_REQUEST_AGAIN;
			} else {
				return IDiagnosable.CODE_REPORT_PROCESSING;
			}
		}*/
		return IDiagnosable.CODE_REPORT_FAILURE;
	}

	@Override
	public int retrieveDiagnoseResult() {
		/*if(report.retrieve()) {
			return IDiagnosable.CODE_REPORT_SUCCESS;
		} else {
			return IDiagnosable.CODE_REPORT_NO_NEW;
		}*/
		return IDiagnosable.CODE_REPORT_NO_NEW;
	}

	@Override
	public boolean updateDiagnoseResult(long reportTime, String content) {
		/*report.setReportTime(reportTime);
		report.setContent(content);
		report.setStatus(BleEcgReport10.DONE);
		return report.updateIfBeing(BleEcgReport10.PROCESS);*/
		return false;
	}
	
	@Override
	public boolean applyProcessingDiagnose() {
		/*report.setStatus(BleEcgReport10.PROCESS);
		return report.updateStatusIfBeing(BleEcgReport10.REQUEST);*/
		return false;
	}
	
	public static BleEcgRecord getFirstRequestRecord() {
		BleEcgReport report = BleEcgReport.getFirstRequestReport();
		if(report != null) {
			return new BleEcgRecord(report.getCreateTime(), report.getDevAddress());
		}
		return null;
	}
}
