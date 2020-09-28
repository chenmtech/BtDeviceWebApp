package com.cmtech.web.btdevice;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;
import com.cmtech.web.dbUtil.IRecordDbOperation;

public abstract class BasicRecord implements IRecord, IRecordDbOperation{
    private String ver; // record version
    private final RecordType type;
    private final long createTime; //
    private final String devAddress; //
    private String creatorPlat;
    private String creatorId;
    private String note;
    
    private final String TABLE_NAME;

    protected BasicRecord(RecordType type, long createTime, String devAddress) {
    	ver = "";
    	this.type = type;
        this.createTime = createTime;
        this.devAddress = devAddress;
        creatorPlat = "";
        creatorId = "";
        note = "";
        TABLE_NAME = getTableName(type);
    }
    
    protected void initFromJson(JSONObject jsonObject) {
		ver = jsonObject.getString("ver");
		if(ver == null || "".equals(ver)) {
			ver = "1.0";
		}
		creatorPlat = jsonObject.getString("creatorPlat");
		creatorId = jsonObject.getString("creatorId");
		note = jsonObject.getString("note");		
    }

    @Override
    public RecordType getType() {
		return type;
	}

	@Override
    public String getVer() {
    	return ver;
    }
    @Override
    public void setVer(String ver) {
        this.ver = ver;
    }
    @Override
    public long getCreateTime() {
        return createTime;
    }
    @Override
    public String getDevAddress() {
        return devAddress;
    }
    @Override
    public String getRecordName() {
        return createTime + devAddress;
    }
    @Override
    public String getCreatorPlat() {
        return creatorPlat;
    }
    @Override
    public String getCreatorId() {
    	return creatorId;
    }
    @Override
    public void setCreator(Account creator) {
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }
    @Override
    public String getNote() {
    	return note;
    }
    @Override
    public void setNote(String note) {
    	this.note = note;
    }

    @Override
    public String toString() {
        return type + "-" + createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + note;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        IRecord other = (IRecord) otherObject;
        return getRecordName().equals(other.getRecordName());
    }

    @Override
    public int hashCode() {
        return getRecordName().hashCode();
    }
    
    @Override
	public JSONObject toJson() {
    	JSONObject json = new JSONObject();
    	json.put("ver", ver);
		json.put("recordTypeCode", type.getCode());
		json.put("createTime", createTime);
		json.put("devAddress", devAddress);
		json.put("creatorPlat", creatorPlat);
		json.put("creatorId", creatorId);
		json.put("note", note);
		return json;
	}

	@Override
    public int getId() {
    	if("".equals(TABLE_NAME)) return INVALID_ID;
    	
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		int id = INVALID_ID;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from " + TABLE_NAME + " where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, devAddress);
			ps.setLong(2, createTime);
			rs = ps.executeQuery();
			if(rs.next()) {
				id = rs.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return id;	
	}
    
	// UPDATE NOTE
    @Override
	public boolean updateNote() {
    	if("".equals(TABLE_NAME)) return false;
    	
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update " + TABLE_NAME + " set note = ? where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, note);
			ps.setString(2, devAddress);
			ps.setLong(3, createTime);
			
			if(ps.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
    
	// DELETE
    @Override
	public boolean delete() {
    	if("".equals(TABLE_NAME)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "delete from " + TABLE_NAME + " where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, devAddress);
			ps.setLong(2, createTime);
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
    
    @Override
    public JSONArray downloadBasicInfo(String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num) {
    	return null;
    }
    
	private static String getTableName(RecordType type) {
		switch(type) {
		case ECG:
			return "ecgrecord";
		case HR:
			return "hrrecord";			
		case THERMO:
			return "thermorecord";
		case TH:
			return "threcord";
		case EEG:
			return "eegrecord";
		default:
			break;
		}
		return "";
	}
}
