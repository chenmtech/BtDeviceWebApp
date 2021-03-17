package com.cmtech.web.btdevice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BlePttRecord extends BasicRecord{
	private static final String[] PROPERTIES = {"sampleRate", "ecgCaliValue", "ecgData", "ppgCaliValue", "ppgData"};
	private int sampleRate; // sample rate
    private int ecgCaliValue; // ecg calibration value
    private String ecgData; // ecg data
    private int ppgCaliValue; // ppg calibration value
    private String ppgData; // ppg data
    
    public BlePttRecord(long createTime, String devAddress) {
    	super(RecordType.PTT, createTime, devAddress);
    }

	public int getSampleRate() {
		return sampleRate;
	}

	public int getEcgCaliValue() {
		return ecgCaliValue;
	}

	public void setEcgCaliValue(int ecgCaliValue) {
		this.ecgCaliValue = ecgCaliValue;
	}

	public int getPpgCaliValue() {
		return ppgCaliValue;
	}

	public void setPpgCaliValue(int ppgCaliValue) {
		this.ppgCaliValue = ppgCaliValue;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setEcgData(String ecgData) {
		this.ecgData = ecgData;
	}

	public void setPpgData(String ppgData) {
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
		ecgCaliValue = json.getInt("ecgCaliValue");
		ecgData = json.getString("ecgData");
		ppgCaliValue = json.getInt("ppgCaliValue");
		ppgData = json.getString("ppgData");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("ecgCaliValue", ecgCaliValue);
		json.put("ecgData", ecgData);
		json.put("ppgCaliValue", ppgCaliValue);
		json.put("ppgData", ppgData);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		ecgCaliValue = rs.getInt("ecgCaliValue");
		ecgData = rs.getString("ecgData");
		ppgCaliValue = rs.getInt("ppgCaliValue");
		ppgData = rs.getString("ppgData");
	}

	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, ecgCaliValue);
		ps.setString(begin++, ecgData);
		ps.setInt(begin++, ppgCaliValue);
		ps.setString(begin++, ppgData);
		return begin;
	}
	
}
