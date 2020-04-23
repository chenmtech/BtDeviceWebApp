package com.cmtech.web.btdevice;

import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cmtech.web.util.MySQLUtil;

public class BleEcgReport10 {
	private int recordId;
	private long modifyTime;
	private String note;

	public BleEcgReport10() {
		
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	public int getId() {
		return getId(recordId);
	}
	
	public static int getId(int recordId) {
		Connection conn = MySQLUtil.connect();
		if(conn == null) return INVALID_ID;
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rlt = null;
		String sql = "select id from ecgreport where recordId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, recordId);
			rlt = ps.executeQuery();
			if(rlt.next()) {
				id = rlt.getInt("id");
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
		return id;		
	}
}
