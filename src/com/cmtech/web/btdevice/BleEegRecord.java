package com.cmtech.web.btdevice;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BleEegRecord extends BasicRecord{
	private static final String[] PROPERTIES = {"leadTypeCode"};
    private int leadTypeCode; // lead type code
    
    public BleEegRecord(int accountId, long createTime, String devAddress) {
    	super(RecordType.EEG, accountId, createTime, devAddress);
    }

	public int getLeadTypeCode() {
		return leadTypeCode;
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
		leadTypeCode = json.getInt("leadTypeCode");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("leadTypeCode", leadTypeCode);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		leadTypeCode = rs.getInt("leadTypeCode");
	}

	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, leadTypeCode);
		return begin;
	}
	
}
