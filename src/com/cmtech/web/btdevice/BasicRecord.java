package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.DEFAULT_VER;
import static com.cmtech.web.MyConstant.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public abstract class BasicRecord implements IDbOperation, IJsonable{
	private static final String[] PROPERTIES = {"createTime", "devAddress", "ver", "creatorId", "note", "recordSecond"};
    private final RecordType type; // record type
    private final long createTime; // record create time
    private final String devAddress; // record device address
    private String ver; // record version
    private int creatorId; // record creator id
    private String note; // record note
    private int recordSecond; // record time length, unit: second

    BasicRecord(RecordType type, long createTime, String devAddress) {
    	this.type = type;
        this.createTime = createTime;
        this.devAddress = devAddress;
    	ver = DEFAULT_VER;
        creatorId = INVALID_ID;
        note = "";
        recordSecond = 0;
    }
    
    public RecordType getType() {
		return type;
	}

    public String getVer() {
    	return ver;
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

    public int getCreatorId() {
    	return creatorId;
    }

    public String getNote() {
    	return note;
    }

    public int getRecordSecond() {
		return recordSecond;
	}
	
	public abstract String[] getProperties();
    
	@Override
    public void fromJson(JSONObject json) {
    	if(json.has("ver")) {
			ver = json.getString("ver");
    	} else {
    		ver = DEFAULT_VER;
    	}
		creatorId = json.getInt("creatorId");
		note = json.getString("note");
		recordSecond = json.getInt("recordSecond");
    }
    
    @Override
	public JSONObject toJson() {
		return basicToJson();
	}
    
    public JSONObject basicToJson() {
    	JSONObject json = new JSONObject();
		json.put("recordTypeCode", type.getCode());
		json.put("createTime", createTime);
		json.put("devAddress", devAddress);
    	json.put("ver", ver);
		json.put("creatorId", creatorId);
		json.put("note", note);
		json.put("recordSecond", recordSecond);
		return json;
    }

	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		readBasicPropertiesFromResultSet(rs);
	}
	
	private void readBasicPropertiesFromResultSet(ResultSet rs) throws SQLException {
		ver = rs.getString("ver");
		creatorId = rs.getInt("creatorId");
		note = rs.getString("note");
		recordSecond = rs.getInt("recordSecond");
	}
	
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = 1;
		ps.setLong(begin++, createTime);
		ps.setString(begin++, devAddress);
		ps.setString(begin++, ver);
		ps.setInt(begin++, creatorId);
		ps.setString(begin++, note);
		ps.setInt(begin++, recordSecond);
		return begin;
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
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return INVALID_ID;	
	}
    
    // INSERT
    @Override
	public final boolean insert() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
    	Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into " + tableName + " (" + getPropertiesString() + ") values (" + getInsertQuestionMark() + ")";
		try {
			ps = conn.prepareStatement(sql);
			writePropertiesToPreparedStatement(ps);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
    
    @Override
	public final boolean retrieve() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select " + getPropertiesString() + " from " + tableName + " where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, createTime);
			ps.setString(2, devAddress);
			rs = ps.executeQuery();
			if(rs.next()) {
				readPropertiesFromResultSet(rs);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;
	}
    
	// DELETE
    @Override
	public final boolean delete() {
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
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
    
    @Override
    public final boolean retrieveBasicInfo() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select " + getBasicPropertiesString() + " from " + tableName + " where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getDevAddress());
			ps.setLong(2, getCreateTime());
			rs = ps.executeQuery();
			if(rs.next()) {
				readBasicPropertiesFromResultSet(rs);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;
	}

	// UPDATE
    @Override
	public boolean update() {
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
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
    
	@Override
    public String toString() {
        return type + "-" + createTime + "-" + devAddress + "-" + creatorId + "-" + note;
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
    
    private String getBasicPropertiesString() {
    	StringBuilder builder = new StringBuilder();
    	int len = PROPERTIES.length;
    	for(int i = 0; i < len-1; i++) {
    		builder.append(PROPERTIES[i]).append(',');
    	}
    	builder.append(PROPERTIES[len-1]);
    	return builder.toString();
    }

    private String getPropertiesString() {
    	StringBuilder builder = new StringBuilder(getBasicPropertiesString());
    	builder.append(',');
    	String[] properties = getProperties();
    	int len = properties.length;
    	for(int i = 0; i < len-1; i++) {
    		builder.append(properties[i]).append(',');
    	}
    	builder.append(properties[len-1]);
    	return builder.toString();
    }
    
    private String getInsertQuestionMark() {
    	int num = PROPERTIES.length + getProperties().length;
    	StringBuilder builder = new StringBuilder();
		for(int i = 0; i < num-1; i++) {
			builder.append('?').append(',');
		}
		builder.append('?');
		return builder.toString();
    }
    
    public static List<BasicRecord> findRecords(RecordType[] types, int creatorId, long fromTime, String noteSearchStr, int num) {
    	if(num <= 0) return null;
		
		List<BasicRecord> found = new ArrayList<>();
		for(RecordType t : types) {
			// 不支持ALL和TH类型的记录
			if(t == RecordType.ALL || t == RecordType.TH) {
				continue;
			}
			List<BasicRecord> tmp = BasicRecord.searchOneTypeRecords(t, creatorId, fromTime, noteSearchStr, num);
			if(tmp != null && !tmp.isEmpty())
				found.addAll(tmp);
		}
		Collections.sort(found, new Comparator<BasicRecord>() {
		    @Override
		    public int compare(BasicRecord o1, BasicRecord o2) {
		    	int rlt = 0;
		        if(o2.getCreateTime() > o1.getCreateTime()) rlt = 1;
		        else if(o2.getCreateTime() < o1.getCreateTime()) rlt = -1;
		        return rlt;
		    }
		});

		int N = Math.min(num, found.size());
		found = found.subList(0, N);
		for(BasicRecord record : found) {
			record.retrieveBasicInfo();
		}
		
		return found;
    }

	private static List<BasicRecord> searchOneTypeRecords(RecordType type, int creatorId, long fromTime, String noteSearchStr, int num) {
		if(num <= 0) return null;
		String tableName = type.getTableName();
		if("".equals(tableName)) return null;
		
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;		
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select createTime, devAddress from " + tableName;
			String where = " where ";
			if(creatorId != INVALID_ID) {
				where += "creatorId = ? and "; 
			}
			if(!"".equals(noteSearchStr)) {
				where += "note REGEXP ? and ";
			}
			where += "createTime < ? order by createTime desc limit ?";
			sql += where;
			
			ps = conn.prepareStatement(sql);
			int i = 1;
			if(creatorId != INVALID_ID) {
				ps.setInt(i++, creatorId);
			}
			if(!"".equals(noteSearchStr)) {
				ps.setString(i++, noteSearchStr);
			}
			ps.setLong(i++, fromTime);
			ps.setInt(i++, num);
			rs = ps.executeQuery();

			List<BasicRecord> found = new ArrayList<>();
			while(rs.next()) {
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				found.add(RecordFactory.create(type, createTime, devAddress));
			}			
			return found;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
}
