package com.cmtech.web.btdevice;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BlePpgRecord extends BasicRecord{
	private static final String[] PROPERTIES = {"sampleRate", "gain"};
	private int sampleRate; // sample rate
    private int gain; // calibration value
    
    public BlePpgRecord(int accountId, long createTime, String devAddress) {
    	super(RecordType.PPG, accountId, createTime, devAddress);
    }

	public int getSampleRate() {
		return sampleRate;
	}

	public int getGain() {
		return gain;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setGain(int gain) {
		this.gain = gain;
	}

	@Override
    public String[] getProperties() {    	
    	return PROPERTIES;
    }
	
	@Override
	public File getSigFilePath() {
		return new File(getSigFileRootPath(), "PPG");
	}	

    @Override
	public void fromJson(JSONObject json) {
		super.fromJson(json);		
		sampleRate = json.getInt("sampleRate");
		gain = json.getInt("gain");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("gain", gain);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		gain = rs.getInt("gain");
	}

	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, gain);
		return begin;
	}
	
}
