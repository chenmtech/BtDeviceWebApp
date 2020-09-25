package com.cmtech.web.btdevice;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleHrRecord10 extends AbstractRecord {
	private String hrList; // list of the filtered HR
    private short hrMax;
    private short hrAve;
    private String hrHist; // HR histogram value
    private int recordSecond; // unit: s

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

	public int getRecordSecond() {
		return recordSecond;
	}

	public void setRecordSecond(int recordSecond) {
		this.recordSecond = recordSecond;
	}
	
	public static BleHrRecord10 createFromJson(JSONObject jsonObject) {
		long createTime = jsonObject.getLong("createTime");
		String devAddress = jsonObject.getString("devAddress");
		BleHrRecord10 record = new BleHrRecord10(createTime, devAddress);
		record.initFromJson(jsonObject);
		
		String hrList = jsonObject.getString("hrList");
		short hrMax = (short) jsonObject.getInt("hrMax");
		short hrAve = (short) jsonObject.getInt("hrAve");
		String hrHist = jsonObject.getString("hrHist");
		int recordSecond = jsonObject.getInt("recordSecond");
		
		record.setHrList(hrList);
		record.setHrMax(hrMax);
		record.setHrAve(hrAve);
		record.setHrHist(hrHist);
		record.setRecordSecond(recordSecond);
		return record;
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("hrList", hrList);
		json.put("hrMax", hrMax);
		json.put("hrAve", hrAve);
		json.put("hrHist", hrHist);
		json.put("recordSecond", recordSecond);	
		return json;
	}

	@Override
	public boolean retrieve() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select creatorPlat, creatorId, note, hrList, hrMax, hrAve, hrHist, recordSecond from hrrecord where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getDevAddress());
			ps.setLong(2, getCreateTime());
			rs = ps.executeQuery();
			if(rs.next()) {
				setCreator(new Account(rs.getString("creatorPlat"), rs.getString("creatorId")));
				setNote(rs.getString("note"));
				hrList = rs.getString("hrList");
				hrMax = rs.getShort("hrMax");
				hrAve = rs.getShort("hrAve");
				hrHist = rs.getString("hrHist");
				recordSecond = rs.getInt("recordSecond");
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
		String sql = "insert into hrrecord (ver, createTime, devAddress, creatorPlat, creatorId, note, hrList, hrMax, hrAve, hrHist, recordSecond) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getVer());
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorId());
			ps.setString(6, getNote());
			ps.setString(7, getHrList());
			ps.setShort(8, getHrMax());
			ps.setShort(9, getHrAve());
			ps.setString(10, getHrHist());
			ps.setInt(11, getRecordSecond());
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
