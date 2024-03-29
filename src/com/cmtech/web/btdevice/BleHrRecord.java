package com.cmtech.web.btdevice;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class BleHrRecord extends BasicRecord {
	private static final String[] PROPERTIES = {"hrList", "hrMax", "hrAve", "hrHist"};
	private String hrList; // list of the filtered HR
    private short hrMax;
    private short hrAve;
    private String hrHist; // HR histogram value

    public BleHrRecord(int accountId, long createTime, String devAddress) {
    	super(RecordType.HR, accountId, createTime, devAddress);
    }

	public String getHrList() {
		return hrList;
	}

	public void setHrList(String hrList) {
		this.hrList = hrList;
	}

	public short getHrMax() {
		return hrMax;
	}

	public void setHrMax(short hrMax) {
		this.hrMax = hrMax;
	}

	public short getHrAve() {
		return hrAve;
	}

	public void setHrAve(short hrAve) {
		this.hrAve = hrAve;
	}

	public String getHrHist() {
		return hrHist;
	}

	public void setHrHist(String hrHist) {
		this.hrHist = hrHist;
	}
	
	@Override
    public String[] getProperties() {    	
    	return PROPERTIES;
    }
	
	@Override
	public File getSigFilePath() {
		return new File(getSigFileRootPath(), "HR");
	}	
    
    @Override
	public void fromJson(JSONObject json) {
		super.fromJson(json);		
		hrList = json.getString("hrList");
		hrMax = (short) json.getInt("hrMax");
		hrAve = (short) json.getInt("hrAve");
		hrHist = json.getString("hrHist");
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("hrList", hrList);
		json.put("hrMax", hrMax);
		json.put("hrAve", hrAve);
		json.put("hrHist", hrHist);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		hrList = rs.getString("hrList");
		hrMax = rs.getShort("hrMax");
		hrAve = rs.getShort("hrAve");
		hrHist = rs.getString("hrHist");
	}
	
	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setString(begin++, hrList);
		ps.setShort(begin++, hrMax);
		ps.setShort(begin++, hrAve);
		ps.setString(begin++, hrHist);
		return begin;
	}
}
