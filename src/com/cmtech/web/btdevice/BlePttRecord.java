package com.cmtech.web.btdevice;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BlePttRecord extends BasicRecord{
	private static final String[] PROPERTIES = {"sampleRate", "ecgCaliValue", "ppgCaliValue"};
	private int sampleRate; // sample rate
    private int ecgCaliValue; // ecg calibration value
    private int ppgCaliValue; // ppg calibration value
    
    public BlePttRecord(int accountId, long createTime, String devAddress) {
    	super(RecordType.PTT, accountId, createTime, devAddress);
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

	@Override
    public String[] getProperties() {    	
    	return PROPERTIES;
    }
	
	@Override
	public File getSigFilePath() {
		return new File(getSigFileRootPath(), "PTT");
	}	

    @Override
	public void fromJson(JSONObject json) {
		super.fromJson(json);		
		sampleRate = json.getInt("sampleRate");
		ecgCaliValue = json.getInt("ecgCaliValue");
		ppgCaliValue = json.getInt("ppgCaliValue");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("ecgCaliValue", ecgCaliValue);
		json.put("ppgCaliValue", ppgCaliValue);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		ecgCaliValue = rs.getInt("ecgCaliValue");
		ppgCaliValue = rs.getInt("ppgCaliValue");
	}

	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, ecgCaliValue);
		ps.setInt(begin++, ppgCaliValue);
		return begin;
	}
	
}
