package com.cmtech.web.btdevice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BleEcgRecord10 extends BasicRecord implements IDiagnosable{
	private static final String[] PROPERTIES = {"sampleRate", "caliValue", "leadTypeCode", "ecgData"};
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private String ecgData; // ecg data
    private BleEcgReport10 report; // ecg diagnose report

    public BleEcgRecord10(long createTime, String devAddress) {
    	super(RecordType.ECG, createTime, devAddress);
    	report = new BleEcgReport10(createTime, devAddress);
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
	
	public JSONObject getReportJson() {
		return report.toJson();
	}
	
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
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("caliValue", caliValue);
		json.put("leadTypeCode", leadTypeCode);
		json.put("ecgData", ecgData);
		json.put("report", report.toJson());
		return json;
	}
	
	@Override
	public void getFromResultSet(ResultSet rs) throws SQLException {
		super.getFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		caliValue = rs.getInt("caliValue");
		leadTypeCode = rs.getInt("leadTypeCode");
		ecgData = rs.getString("ecgData");
	}
	
	@Override
	public int setToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.setToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, caliValue);
		ps.setInt(begin++, leadTypeCode);
		ps.setString(begin++, ecgData);
		return begin;
	}

	@Override
	public boolean retrieve() {
		report.retrieve();
		return super.retrieve();
	}

	@Override
	public boolean delete() {
		report.delete();
		return super.delete();
	}
	
	@Override
	public int requestDiagnose() {
		if(!report.retrieve()) {
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
		}
	}

	@Override
	public int retrieveDiagnoseResult() {
		if(report.retrieve()) {
			return IDiagnosable.CODE_REPORT_SUCCESS;
		} else {
			return IDiagnosable.CODE_REPORT_NO_NEW;
		}
	}

	@Override
	public boolean updateDiagnoseResult(long reportTime, String content) {
		report.setReportTime(reportTime);
		report.setContent(content);
		report.setStatus(BleEcgReport10.DONE);
		return report.updateIfBeing(BleEcgReport10.PROCESS);
	}
	
	@Override
	public boolean applyForDiagnose() {
		report.setStatus(BleEcgReport10.PROCESS);
		return report.updateStatusIfBeing(BleEcgReport10.REQUEST);
	}
	
	public static BleEcgRecord10 getFirstRequestRecord() {
		BleEcgReport10 report = BleEcgReport10.getFirstRequestReport();
		if(report != null) {
			return new BleEcgRecord10(report.getCreateTime(), report.getDevAddress());
		}
		return null;
	}
}
