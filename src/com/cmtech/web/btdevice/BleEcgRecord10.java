package com.cmtech.web.btdevice;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEcgRecord10 extends AbstractRecord{
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private int recordSecond; // unit: s
    private String ecgData; // ecg data
    
    public BleEcgRecord10() {
    	this(0, "");
    }
    
    public BleEcgRecord10(long createTime, String devAddress) {
    	super(RecordType.ECG, createTime, devAddress);
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

	public int getRecordSecond() {
		return recordSecond;
	}

	public String getEcgData() {
		return ecgData;
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

	public void setRecordSecond(int recordSecond) {
		this.recordSecond = recordSecond;
	}

	public void setEcgData(String ecgData) {
		this.ecgData = ecgData;
	}
	
	public static BleEcgRecord10 createFromJson(JSONObject jsonObject) {
		String ver = jsonObject.getString("ver");
		long createTime = jsonObject.getLong("createTime");
		String devAddress = jsonObject.getString("devAddress");
		String creatorPlat = jsonObject.getString("creatorPlat");
		String creatorId = jsonObject.getString("creatorId");
		String note = jsonObject.getString("note");
		int sampleRate = jsonObject.getInt("sampleRate");
		int caliValue = jsonObject.getInt("caliValue");
		int leadTypeCode = jsonObject.getInt("leadTypeCode");
		int recordSecond = jsonObject.getInt("recordSecond");
		String ecgData = jsonObject.getString("ecgData");
		
		BleEcgRecord10 record = new BleEcgRecord10();
		if("".equals(ver)) {
			ver = "1.0";
		}
		record.setVer(ver);
		record.setCreateTime(createTime);
		record.setDevAddress(devAddress);
		record.setCreator(new Account(creatorPlat, creatorId));
		record.setNote(note);
		record.setSampleRate(sampleRate);
		record.setCaliValue(caliValue);
		record.setLeadTypeCode(leadTypeCode);
		record.setRecordSecond(recordSecond);
		record.setEcgData(ecgData);
		return record;
	}
	
	@Override
	public boolean insert() {
		int id = retrieveId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into ecgrecord (ver, createTime, devAddress, creatorPlat, creatorId, note, sampleRate, caliValue, leadTypeCode, recordSecond, ecgData) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getVer());
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorPlatId());
			ps.setString(6, getNote());
			ps.setInt(7, getSampleRate());
			ps.setInt(8, getCaliValue());
			ps.setInt(9, getLeadTypeCode());
			ps.setInt(10, getRecordSecond());
			ps.setString(11, getEcgData());
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
