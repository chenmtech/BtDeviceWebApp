package com.cmtech.web.btdevice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.cmtech.web.util.MySQLUtil;
import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

public class BleEcgRecord10 extends AbstractRecord{
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private int recordSecond; // unit: s
    private String note; // record description
    private List<Short> ecgData; // ecg data
    
    public BleEcgRecord10() {
    	super();
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

	public String getNote() {
		return note;
	}

	public List<Short> getEcgData() {
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

	public void setNote(String note) {
		this.note = note;
	}

	public void setEcgData(List<Short> ecgData) {
		this.ecgData = ecgData;
	}

	public int getId() {
		Connection conn = MySQLUtil.getConnection();
		if(conn == null) return INVALID_ID;
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rlt = null;
		String sql = "select id from ecgrecord where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getDevAddress());
			ps.setLong(2, getCreateTime());
			rlt = ps.executeQuery();
			if(rlt.next()) {
				id = rlt.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(rlt != null)
				try {
					rlt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return id;		
	}
	
	public boolean insert() {
		int id = getId();
		if(id != INVALID_ID) return false;
		
		Connection conn = MySQLUtil.getConnection();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into ecgrecord (ver, createTime, devAddress, creatorPlat, creatorId, sampleRate, caliValue) values (?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, Arrays.toString(getVer()));
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorPlatId());
			ps.setInt(6, sampleRate);
			ps.setInt(7, caliValue);
			
			boolean rlt = ps.execute();
			if(!rlt && ps.getUpdateCount() == 1)
				return true;
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return false;
	}
}
