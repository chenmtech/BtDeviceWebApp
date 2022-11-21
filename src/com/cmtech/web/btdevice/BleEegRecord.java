package com.cmtech.web.btdevice;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BleEegRecord extends BasicRecord{
	private static final String[] PROPERTIES = {"sampleRate", "gain", "leadTypeCode"};
	private int sampleRate; // sample rate
    private int gain; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    
    public BleEegRecord(int accountId, long createTime, String devAddress) {
    	super(RecordType.EEG, accountId, createTime, devAddress);
    }

	public int getSampleRate() {
		return sampleRate;
	}

	public int getGain() {
		return gain;
	}

	public int getLeadTypeCode() {
		return leadTypeCode;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setGain(int gain) {
		this.gain = gain;
	}

	public void setLeadTypeCode(int leadTypeCode) {
		this.leadTypeCode = leadTypeCode;
	}

	@Override
    public String[] getProperties() {    	
    	return PROPERTIES;
    }
	
	@Override
	public File getSigFilePath() {
		return new File(getSigFileRootPath(), "EEG");
	}	

    @Override
	public void fromJson(JSONObject json) {
		super.fromJson(json);		
		sampleRate = json.getInt("sampleRate");
		gain = json.getInt("gain");
		leadTypeCode = json.getInt("leadTypeCode");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("gain", gain);
		json.put("leadTypeCode", leadTypeCode);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		gain = rs.getInt("gain");
		leadTypeCode = rs.getInt("leadTypeCode");
	}

	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, gain);
		ps.setInt(begin++, leadTypeCode);
		return begin;
	}
	
}
