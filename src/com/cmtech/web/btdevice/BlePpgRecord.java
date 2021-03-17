package com.cmtech.web.btdevice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BlePpgRecord extends BasicRecord{
	private static final String[] PROPERTIES = {"sampleRate", "caliValue", "ppgData"};
	private int sampleRate; // sample rate
    private int caliValue; // calibration value
    private String ppgData; // ppg data
    
    public BlePpgRecord(long createTime, String devAddress) {
    	super(RecordType.PPG, createTime, devAddress);
    }

	public int getSampleRate() {
		return sampleRate;
	}

	public int getCaliValue() {
		return caliValue;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setCaliValue(int caliValue) {
		this.caliValue = caliValue;
	}

	public void setEegData(String ppgData) {
		this.ppgData = ppgData;
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
		ppgData = json.getString("ppgData");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("caliValue", caliValue);
		json.put("ppgData", ppgData);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		caliValue = rs.getInt("caliValue");
		ppgData = rs.getString("ppgData");
	}

	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, caliValue);
		ps.setString(begin++, ppgData);
		return begin;
	}
	
}
