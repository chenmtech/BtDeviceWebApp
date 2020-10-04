package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleHrRecord10 extends BasicRecord {
	private static final String SELECT_STR = BasicRecord.SELECT_STR + "hrList, hrMax, hrAve, hrHist";
	private String hrList; // list of the filtered HR
    private short hrMax;
    private short hrAve;
    private String hrHist; // HR histogram value

    public BleHrRecord10(long createTime, String devAddress) {
    	super(RecordType.HR, createTime, devAddress);
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
    
    public String getSelectStr() {
		return SELECT_STR;
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
	public void setFromResultSet(ResultSet rs) throws SQLException {
		super.setFromResultSet(rs);
		hrList = rs.getString("hrList");
		hrMax = rs.getShort("hrMax");
		hrAve = rs.getShort("hrAve");
		hrHist = rs.getString("hrHist");
	}

	@Override
	public boolean insert() {
		int id = getId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into HrRecord (ver, createTime, devAddress, creatorPlat, creatorId, note, recordSecond, hrList, hrMax, hrAve, hrHist) "
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
			ps.setString(8, hrList);
			ps.setShort(9, hrMax);
			ps.setShort(10, hrAve);
			ps.setString(11, hrHist);
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
