package com.cmtech.web.btdevice;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BlePttRecord extends BasicRecord{
	private static final String[] PROPERTIES = new String[0];
    
    public BlePttRecord(int accountId, long createTime, String devAddress) {
    	super(RecordType.PTT, accountId, createTime, devAddress);
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
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
	}

	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		return begin;
	}
	
}
