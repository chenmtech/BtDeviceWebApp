package com.cmtech.web.btdevice;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BlePttRecord extends BasicRecord{
	private static final String[] PROPERTIES = {"sampleRate", "ecgGain", "ppgGain"};
	private int sampleRate; // sample rate
    private int ecgGain; // ecg calibration value
    private int ppgGain; // ppg calibration value
    
    public BlePttRecord(int accountId, long createTime, String devAddress) {
    	super(RecordType.PTT, accountId, createTime, devAddress);
    }

	public int getSampleRate() {
		return sampleRate;
	}

	public int getEcgGain() {
		return ecgGain;
	}

	public void setEcgGain(int ecgGain) {
		this.ecgGain = ecgGain;
	}

	public int getPpgGain() {
		return ppgGain;
	}

	public void setPpgGain(int ppgGain) {
		this.ppgGain = ppgGain;
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
		ecgGain = json.getInt("ecgGain");
		ppgGain = json.getInt("ppgGain");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("ecgGain", ecgGain);
		json.put("ppgGain", ppgGain);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		ecgGain = rs.getInt("ecgGain");
		ppgGain = rs.getInt("ppgGain");
	}

	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, ecgGain);
		ps.setInt(begin++, ppgGain);
		return begin;
	}
	
}
