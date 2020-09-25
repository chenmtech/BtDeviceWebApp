package com.cmtech.web.btdevice;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleThermoRecord10 extends AbstractRecord {
    private String temp;
    
    public BleThermoRecord10(long createTime, String devAddress) {
    	super(RecordType.THERMO, createTime, devAddress);
    }
    
	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}
    
	public static BleThermoRecord10 createFromJson(JSONObject jsonObject) {
		long createTime = jsonObject.getLong("createTime");
		String devAddress = jsonObject.getString("devAddress");
		BleThermoRecord10 record = new BleThermoRecord10(createTime, devAddress);
		record.initFromJson(jsonObject);
		
		String temp = jsonObject.getString("temp");
		record.setTemp(temp);
		return record;
	}	

	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("temp", temp);	
		return json;
	}

	@Override
	public boolean retrieve() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select creatorPlat, creatorId, note, temp from thermorecord where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getDevAddress());
			ps.setLong(2, getCreateTime());
			rs = ps.executeQuery();
			if(rs.next()) {
				setCreator(new Account(rs.getString("creatorPlat"), rs.getString("creatorId")));
				setNote(rs.getString("note"));
				temp = rs.getString("temp");
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
		int id = retrieveId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into thermorecord (ver, createTime, devAddress, creatorPlat, creatorId, note, temp) "
				+ "values (?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getVer());
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorId());
			ps.setString(6, getNote());
			ps.setString(7, getTemp());
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
