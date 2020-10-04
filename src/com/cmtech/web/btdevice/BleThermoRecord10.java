package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleThermoRecord10 extends BasicRecord {
	private static final String SELECT_STR = BasicRecord.SELECT_STR + "temp";
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
    
    public String getSelectStr() {
		return SELECT_STR;
	}

    @Override
	public void fromJson(JSONObject json) {
		super.fromJson(json);		
		temp = json.getString("temp");
	}	

	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("temp", temp);	
		return json;
	}
	
	@Override
	public void setFromResultSet(ResultSet rs) throws SQLException {
		super.setFromResultSet(rs);
		temp = rs.getString("temp");
	}
	
	@Override
	public boolean insert() {
		int id = getId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into ThermoRecord (ver, createTime, devAddress, creatorPlat, creatorId, note, recordSecond, temp) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getVer());
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorId());
			ps.setString(6, getNote());
			ps.setInt(7, getRecordSecond());
			ps.setString(8, temp);
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
