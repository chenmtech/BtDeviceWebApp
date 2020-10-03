package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEegRecord10 extends BasicRecord{
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private String eegData; // ecg data
    
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

    @Override
	public void fromJson(JSONObject jsonObject) {
		super.fromJson(jsonObject);
		
		sampleRate = jsonObject.getInt("sampleRate");
		caliValue = jsonObject.getInt("caliValue");
		leadTypeCode = jsonObject.getInt("leadTypeCode");
		eegData = jsonObject.getString("eegData");
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
	public boolean retrieve() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select creatorPlat, creatorId, note, recordSecond, sampleRate, caliValue, leadTypeCode, eegData from EegRecord "
				+ "where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, getCreateTime());
			ps.setString(2, getDevAddress());
			rs = ps.executeQuery();
			if(rs.next()) {
				setCreator(new Account(rs.getString("creatorPlat"), rs.getString("creatorId")));
				setNote(rs.getString("note"));
				setRecordSecond(rs.getInt("recordSecond"));
				sampleRate = rs.getInt("sampleRate");
				caliValue = rs.getInt("caliValue");
				leadTypeCode = rs.getInt("leadTypeCode");
				eegData = rs.getString("eegData");
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;
	}
	
	

	@Override
	public boolean insert() {
		int id = getId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into EegRecord (ver, createTime, devAddress, creatorPlat, creatorId, note, sampleRate, caliValue, leadTypeCode, recordSecond, eegData) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getVer());
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorId());
			ps.setString(6, getNote());
			ps.setInt(7, getSampleRate());
			ps.setInt(8, getCaliValue());
			ps.setInt(9, getLeadTypeCode());
			ps.setInt(10, getRecordSecond());
			ps.setString(11, getEegData());			
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
