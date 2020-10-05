package com.cmtech.web.btdevice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BleEegRecord10 extends BasicRecord{
	private static final String[] PROPERTIES = {"sampleRate", "caliValue", "leadTypeCode", "eegData"};
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private String eegData; // eeg data
    
    public BleEegRecord10(long createTime, String devAddress) {
    	super(RecordType.EEG, createTime, devAddress);
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

	public String getEegData() {
		return eegData;
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

	public void setEegData(String eegData) {
		this.eegData = eegData;
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
		eegData = json.getString("eegData");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("caliValue", caliValue);
		json.put("leadTypeCode", leadTypeCode);
		json.put("eegData", eegData);
		return json;
	}
	
	@Override
	public void getPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.getPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		caliValue = rs.getInt("caliValue");
		leadTypeCode = rs.getInt("leadTypeCode");
		eegData = rs.getString("eegData");
	}

	@Override
	public int setPropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.setPropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, caliValue);
		ps.setInt(begin++, leadTypeCode);
		ps.setString(begin++, eegData);
		return begin;
	}
	
}
