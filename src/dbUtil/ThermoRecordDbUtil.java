package dbUtil;

import static dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.BleThermoRecord10;
import com.cmtech.web.btdevice.RecordType;

public class ThermoRecordDbUtil {
	
	public static boolean upload(JSONObject json) {
		BleThermoRecord10 record = createFromJson(json);
		if(record == null) return false;
		
		int id = RecordDbUtil.query(RecordType.THERMO, record.getCreateTime(), record.getDevAddress());
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into thermorecord (ver, createTime, devAddress, creatorPlat, creatorId, note, temp) "
				+ "values (?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, record.getVer());
			ps.setLong(2, record.getCreateTime());
			ps.setString(3, record.getDevAddress());
			ps.setString(4, record.getCreatorPlat());
			ps.setString(5, record.getCreatorPlatId());
			ps.setString(6, record.getNote());
			ps.setString(7, record.getTemp());
			
			boolean rlt = ps.execute();
			if(!rlt && ps.getUpdateCount() == 1)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	public static JSONObject downloadBasicInfo(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ver, createTime, devAddress, note from thermorecord where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				String ver = rs.getString("ver");
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				String note = rs.getString("note");
				JSONObject json = new JSONObject();
				json.put("recordTypeCode", RecordType.THERMO.getCode());
				json.put("ver", ver);
				json.put("createTime", createTime);
				json.put("devAddress", devAddress);
				json.put("note", note);
			
				return json;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
	
	public static JSONObject download(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select createTime, devAddress, creatorPlat, creatorId, note, temp from thermorecord where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				String creatorPlat = rs.getString("creatorPlat");
				String creatorId = rs.getString("creatorId");
				String note = rs.getString("note");
				String temp = rs.getString("temp");
				JSONObject json = new JSONObject();
				json.put("recordTypeCode", RecordType.THERMO.getCode());
				json.put("createTime", createTime);
				json.put("devAddress", devAddress);
				json.put("creatorPlat", creatorPlat);
				json.put("creatorId", creatorId);
				json.put("note", note);
				json.put("temp", temp);
			
				return json;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
	
	private static BleThermoRecord10 createFromJson(JSONObject jsonObject) {
		String ver = jsonObject.getString("ver");
		long createTime = jsonObject.getLong("createTime");
		String devAddress = jsonObject.getString("devAddress");
		String creatorPlat = jsonObject.getString("creatorPlat");
		String creatorId = jsonObject.getString("creatorId");
		String note = jsonObject.getString("note");
		String temp = jsonObject.getString("temp");
		
		BleThermoRecord10 record = new BleThermoRecord10();
		if("".equals(ver)) {
			ver = "1.0";
		}
		record.setVer(ver);
		record.setCreateTime(createTime);
		record.setDevAddress(devAddress);
		record.setCreator(new Account(creatorPlat, creatorId));
		record.setNote(note);
		record.setTemp(temp);
		return record;
	}
}
