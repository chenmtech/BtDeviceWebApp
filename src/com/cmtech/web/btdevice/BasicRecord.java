package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public abstract class BasicRecord implements IDbOperation, IJsonable{
	public static final String SELECT_STR = "creatorPlat, creatorId, note, recordSecond, ";
    private String ver; // record version
    private final RecordType type;
    private final long createTime; //
    private final String devAddress; //
    private String creatorPlat;
    private String creatorId;
    private String note;
    private int recordSecond;

    protected BasicRecord(RecordType type, long createTime, String devAddress) {
    	ver = DEFAULT_VER;
    	this.type = type;
        this.createTime = createTime;
        this.devAddress = devAddress;
        creatorPlat = "";
        creatorId = "";
        note = "";
        recordSecond = 0;
    }    
    
    public RecordType getType() {
		return type;
	}

    public String getVer() {
    	return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getDevAddress() {
        return devAddress;
    }

    public String getRecordName() {
        return createTime + devAddress;
    }

    public String getCreatorPlat() {
        return creatorPlat;
    }

    public String getCreatorId() {
    	return creatorId;
    }

    public void setCreator(Account creator) {
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }

    public String getNote() {
    	return note;
    }

    public void setNote(String note) {
    	this.note = note;
    }

    public int getRecordSecond() {
		return recordSecond;
	}

	public void setRecordSecond(int recordSecond) {
		this.recordSecond = recordSecond;
	}
    
    public abstract String getSelectStr();

	@Override
    public void fromJson(JSONObject json) {
    	if(json.has("ver")) {
			ver = json.getString("ver");			
    	} else {
    		ver = DEFAULT_VER;
    	}
		creatorPlat = json.getString("creatorPlat");
		creatorId = json.getString("creatorId");
		note = json.getString("note");
		recordSecond = json.getInt("recordSecond");
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
		json.put("recordSecond", recordSecond);
		return json;
	}

	@Override
	public void setFromResultSet(ResultSet rs) throws SQLException {
		creatorPlat = rs.getString("creatorPlat");
		creatorId = rs.getString("creatorId");
		note = rs.getString("note");
		recordSecond = rs.getInt("recordSecond");
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
        BasicRecord other = (BasicRecord) otherObject;
        return getRecordName().equals(other.getRecordName());
    }

    @Override
    public int hashCode() {
        return getRecordName().hashCode();
    }

    @Override
    public final int getId() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return INVALID_ID;
    	
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from " + tableName + " where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, createTime);
			ps.setString(2, devAddress);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return INVALID_ID;	
	}
    
    @Override
	public boolean retrieve() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select " + getSelectStr() + " from " + tableName + " where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getDevAddress());
			ps.setLong(2, getCreateTime());
			rs = ps.executeQuery();
			if(rs.next()) {
				setFromResultSet(rs);
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
    
	// UPDATE NOTE
    @Override
	public final boolean update() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update " + tableName + " set note = ? where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, note);
			ps.setLong(2, createTime);
			ps.setString(3, devAddress);			
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
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "delete from " + tableName + " where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, createTime);
			ps.setString(2, devAddress);
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
