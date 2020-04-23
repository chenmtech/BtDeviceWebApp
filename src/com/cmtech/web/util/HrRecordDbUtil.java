package com.cmtech.web.util;

import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.BleHrRecord10;
import com.cmtech.web.btdevice.RecordType;

public class HrRecordDbUtil {
	public static boolean upload(JSONObject json) {
		BleHrRecord10 record = parseJson(json);
		
		int id = RecordDbUtil.query(RecordType.HR, record.getCreateTime(), record.getDevAddress());
		if(id != INVALID_ID) return false;
		
		Connection conn = MySQLUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into hrrecord (ver, createTime, devAddress, creatorPlat, creatorId, filterHrList, hrMax, hrMin, hrHist, recordSecond) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, record.getVer());
			ps.setLong(2, record.getCreateTime());
			ps.setString(3, record.getDevAddress());
			ps.setString(4, record.getCreatorPlat());
			ps.setString(5, record.getCreatorPlatId());
			ps.setString(6, record.getFilterHrList());
			ps.setShort(7, record.getHrMax());
			ps.setShort(8, record.getHrAve());
			ps.setString(9, record.getHrHist());
			ps.setInt(10, record.getRecordSecond());
			
			boolean rlt = ps.execute();
			if(!rlt && ps.getUpdateCount() == 1)
				return true;
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
			
			MySQLUtil.disconnect(conn);
		}
		return false;
	}

	public static JSONObject download(int id) {
		Connection conn = MySQLUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rlt = null;
		String sql = "select createTime, devAddress, creatorPlat, creatorId, filterHrList, hrMax, hrAve, hrHist, recordSecond from hrrecord where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rlt = ps.executeQuery();
			if(rlt.next()) {
				long createTime = rlt.getLong("createTime");
				String devAddress = rlt.getString("devAddress");
				String creatorPlat = rlt.getString("creatorPlat");
				String creatorId = rlt.getString("creatorId");
				String filterHrList = rlt.getString("filterHrList");
				short hrMax = rlt.getShort("hrMax");
				short hrAve = rlt.getShort("hrAve");
				String hrHist = rlt.getString("hrHist");
				int recordSecond = rlt.getInt("recordSecond");
				JSONObject json = new JSONObject();
				json.put("createTime", createTime);
				json.put("devAddress", devAddress);
				json.put("creatorPlat", creatorPlat);
				json.put("creatorId", creatorId);
				json.put("filterHrList", filterHrList);
				json.put("hrMax", hrMax);
				json.put("hrAve", hrAve);
				json.put("hrHist", hrHist);
				json.put("recordSecond", recordSecond);
			
				return json;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(rlt != null)
				try {
					rlt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			MySQLUtil.disconnect(conn);
		}
		return null;
	}
	
	private static BleHrRecord10 parseJson(JSONObject jsonObject) {
		String ver = jsonObject.getString("ver");
		long createTime = jsonObject.getLong("createTime");
		String devAddress = jsonObject.getString("devAddress");
		String creatorPlat = jsonObject.getString("creatorPlat");
		String creatorId = jsonObject.getString("creatorId");
		String filterHrList = jsonObject.getString("filterHrList");
		short hrMax = (short) jsonObject.getInt("hrMax");
		short hrAve = (short) jsonObject.getInt("hrAve");
		String hrHist = jsonObject.getString("hrHist");
		int recordSecond = jsonObject.getInt("recordSecond");

		BleHrRecord10 record = new BleHrRecord10();
		record.setVer(ver);
		record.setCreateTime(createTime);
		record.setDevAddress(devAddress);
		record.setCreator(new Account(creatorPlat, creatorId));
		record.setFilterHrList(filterHrList);
		record.setHrMax(hrMax);
		record.setHrAve(hrAve);
		record.setHrHist(hrHist);
		record.setRecordSecond(recordSecond);
		return record;
	}
}
