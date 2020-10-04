package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEegRecord10 extends BasicRecord{
	private static final String SELECT_STR = BasicRecord.SELECT_STR + "sampleRate, caliValue, leadTypeCode, eegData";
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
    
    public String getSelectStr() {
		return SELECT_STR;
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
	public void setFromResultSet(ResultSet rs) throws SQLException {
		super.setFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		caliValue = rs.getInt("caliValue");
		leadTypeCode = rs.getInt("leadTypeCode");
		eegData = rs.getString("eegData");
	}

	@Override
	public boolean insert() {
		int id = getId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into EegRecord (ver, createTime, devAddress, creatorPlat, creatorId, note, recordSecond, sampleRate, caliValue, leadTypeCode, eegData) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getVer());
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorId());
			ps.setString(6, getNote());
			ps.setInt(7, getRecordSecond());
			ps.setInt(8, sampleRate);
			ps.setInt(9, caliValue);
			ps.setInt(10, leadTypeCode);
			ps.setString(11, eegData);			
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}	
	
}
