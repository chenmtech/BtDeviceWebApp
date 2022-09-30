package com.cmtech.web.btdevice;


import static com.cmtech.web.MyConstant.INVALID_ID;
import static com.cmtech.web.MyConstant.INVALID_TIME;

import java.io.File;
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

/**
 * 所有记录的基础类，包含各种记录共有的属性和基本操作
 * @author gdmc
 *
 */
public abstract class BasicRecord implements IDbOperation, IJsonable{
	//------------------------------------------------常量
	// 基本记录中可以进行数据库读写的属性字段字符串数组
	private static final String[] DB_PROPERTIES = {"createTime", "devAddress", "ver", "creatorId", "note", 
			"recordSecond", "reportVer", "reportProvider", "reportTime", "reportContent", "reportStatus"};
	
	// 缺省记录版本号
	private static final String DEFAULT_RECORD_VER = "1.0";
	
	// 报告的缺省版本号
	private static final String DEFAULT_REPORT_VER = "0.0";	
	
    // 缺省报告提供者
    private static final String DEFAULT_REPORT_PROVIDER = "";
	
	// 报告的状态
	public static final int REPORT_STATUS_DONE = 0; // 已完成
	
    public static final int REPORT_STATUS_PROCESSING = 1; // 处理中    
    	
    
    //------------------------------------------------------实例变量
	// 记录类型
	private final RecordType type;
	
	// 记录创建时间
    private final long createTime;
    
    // 记录获取设备蓝牙地址
    private final String devAddress;
    
    // 记录版本号
    private String ver;

    // 记录创建者ID
    private int creatorId;
    
    // 记录备注
    private String note;
    
    // 记录信号长度：秒数
    private int recordSecond;
    
    // 诊断报告相关字段 
    
    // 报告版本号
    private String reportVer = DEFAULT_REPORT_VER;
    
    // 报告提供者
    private String reportProvider = DEFAULT_REPORT_PROVIDER;
    
    // 报告产生的时间
    private long reportTime = INVALID_TIME;
    
    // 报告产生的内容
    private String reportContent = ""; 

    // 报告的状态
    private int reportStatus = REPORT_STATUS_DONE;    

    
    //--------------------------------------------------------静态变量
    // 信号文件存放路径
    private static File sigFilePath = new File(System.getProperty("catalina.home")+File.separator + "MY_FILE");
    
    
    
    //-----------------------------------------------------静态函数
    
    /**
     * 设置信号文件存储路径
     * @param sigPath
     */
    public static void setSigFilePath(File sigPath) {
    	sigFilePath = sigPath;
    }    
    

    /**
     * 用于从数据库中查找满足条件的记录，按创建时间排序，并获取记录信息
     * @param types：要查找的记录类型
     * @param creatorId：记录创建者ID
     * @param fromTime：记录起始创建时间
     * @param filterStr：备注中包含的字符串
     * @param num：查找的记录数
     * @return：得到的记录列表
     */
    public static List<BasicRecord> retrieveRecordList(RecordType[] types, int creatorId, long fromTime, String filterStr, int num) {
    	if(num <= 0) return null;
		
		List<BasicRecord> found = new ArrayList<>();
		for(RecordType t : types) {
			// 不支持ALL和TH类型的记录
			if(t == RecordType.ALL || t == RecordType.TH) {
				continue;
			}
			List<BasicRecord> tmp = BasicRecord.searchOneTypeRecordList(t, creatorId, fromTime, filterStr, num);
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
			record.retrieve();
		}
		
		return found;
    }

    // 获取一种类型的记录列表，仅包含createTime和devAddress字段信息
	private static List<BasicRecord> searchOneTypeRecordList(RecordType type, int creatorId, long fromTime, String filterStr, int num) {
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
			if(!"".equals(filterStr)) {
				where += "note REGEXP ? and ";
			}
			where += "createTime < ? order by createTime desc limit ?";
			sql += where;
			
			ps = conn.prepareStatement(sql);
			int i = 1;
			if(creatorId != INVALID_ID) {
				ps.setInt(i++, creatorId);
			}
			if(!"".equals(filterStr)) {
				ps.setString(i++, filterStr);
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
    
	//------------------------------------------------------构造器
    BasicRecord(RecordType type, long createTime, String devAddress) {
    	this.type = type;
        this.createTime = createTime;
        this.devAddress = devAddress;
    	ver = DEFAULT_RECORD_VER;
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
    
	public String getReportVer() {
		return reportVer;
	}

	public String getReportProvider() {
		return reportProvider;
	}

	public long getReportTime() {
		return reportTime;
	}

	public String getReportContent() {
		return reportContent;
	}

	public int getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(int status) {
		this.reportStatus = status;
	}

	public File getSigFilePath() {
		return sigFilePath;
	}
	
	public String getSigFileName() {
        return getDevAddress().replace(":", "")+getCreateTime();
    }
	
	// 获取记录中要进行数据库操作的属性字段字符串数组，不包括BasicRecord中的字段
	public abstract String[] getProperties();
	
    
	@Override
    public void fromJson(JSONObject json) {
		ver = json.getString("ver");
		creatorId = json.getInt("creatorId");
		note = json.getString("note");
		recordSecond = json.getInt("recordSecond");
		
		reportVer = json.getString("reportVer");
		reportProvider = json.getString("reportProvider");
		reportTime = json.getLong("reportTime");
		reportContent = json.getString("reportContent");
		reportStatus = json.getInt("reportStatus");
    }
    
    @Override
	public JSONObject toJson() {
    	JSONObject json = new JSONObject();
		json.put("recordTypeCode", type.getCode());
		json.put("createTime", createTime);
		json.put("devAddress", devAddress);
    	json.put("ver", ver);
		json.put("creatorId", creatorId);
		json.put("note", note);
		json.put("recordSecond", recordSecond);
		json.put("reportVer", reportVer);
		json.put("reportProvider", reportProvider);
		json.put("reportTime", reportTime);
		json.put("reportContent", reportContent);
		json.put("reportStatus", reportStatus);
		return json;
	}
    

    /**
     * 从数据库操作的ResultSet读取属性值
     * @param rs
     * @throws SQLException
     */
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		ver = rs.getString("ver");
		creatorId = rs.getInt("creatorId");
		note = rs.getString("note");
		recordSecond = rs.getInt("recordSecond");
		reportVer = rs.getString("reportVer");
		reportProvider = rs.getString("reportProvider");
		reportTime = rs.getLong("reportTime");
		reportContent = rs.getString("reportContent");
		reportStatus = rs.getInt("reportStatus");
	}	
	
	/**
	 * 将属性值写入数据库操作的PreparedStatement
	 * 注意：写入的字段顺序必须和getPropertiesString()返回的字段顺序一致
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = 1;
		ps.setLong(begin++, createTime);
		ps.setString(begin++, devAddress);
		ps.setString(begin++, ver);
		ps.setInt(begin++, creatorId);
		ps.setString(begin++, note);
		ps.setInt(begin++, recordSecond);
		ps.setString(begin++, reportVer);
		ps.setString(begin++, reportProvider);
		ps.setLong(begin++, reportTime);
		ps.setString(begin++, reportContent);
		ps.setInt(begin++, reportStatus);
		return begin;
	}

	/**
	 * 获取该记录在数据表中的ID：用type, createTime和devAddress
	 */
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
    
    /**
     * 将该记录插入到数据库中
     */
    @Override
	public final boolean insert() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
    	Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into " + tableName + " (" + getDbPropertiesString() + ") values (" + getInsertQuestionMark() + ")";
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
    
    /**
     * 从数据库中获取该记录的属性字段值，属性应包含于getPropertiesString()的字符串中
     */
    @Override
	public final boolean retrieve() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select " + getDbPropertiesString() + " from " + tableName + " where createTime = ? and devAddress = ?";
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
    
	/**
	 * 从数据库中删除该记录
	 */
    @Override
	public boolean delete() {
    	boolean success = false;
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
				success = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		
		// 删除信号文件
		if(success) {
			File file = new File(getSigFilePath(), getSigFileName());
			if(file.exists()) success = file.delete();
		}
		return success;
	}

	/**
	 * 更新记录，目前只能更新note字段
	 */
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
    
    protected boolean updateReportStatus(RecordType type, int fromStatus, int toStatus) {
		String tableName = type.getTableName();
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update " + tableName +" set reportStatus = ? where createTime = ? and devAddress = ? and reportStatus = ?";
		try {
			int begin = 1;
			ps = conn.prepareStatement(sql);
			ps.setInt(begin++, toStatus);
			ps.setLong(begin++, getCreateTime());
			ps.setString(begin++, getDevAddress());
			ps.setInt(begin++, fromStatus);
			if(ps.executeUpdate() != 0) {
				reportStatus = toStatus;
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
    
    /**
     * 获取基本属性字段用于数据库操作的String
     * @return：比如"createTime, devAddress"
     */
    private String getBasicDbPropertiesString() {
    	StringBuilder builder = new StringBuilder();
    	int len = DB_PROPERTIES.length;
    	for(int i = 0; i < len-1; i++) {
    		builder.append(DB_PROPERTIES[i]).append(',');
    	}
    	builder.append(DB_PROPERTIES[len-1]);
    	return builder.toString();
    }

    /**
     * 获取属性字段用于数据库操作的String
     * @return
     */
    private String getDbPropertiesString() {
    	StringBuilder builder = new StringBuilder(getBasicDbPropertiesString());
    	builder.append(',');
    	String[] properties = getProperties();
    	int len = properties.length;
    	for(int i = 0; i < len-1; i++) {
    		builder.append(properties[i]).append(',');
    	}
    	builder.append(properties[len-1]);
    	return builder.toString();
    }
    
    /**
     * 获取属性字段用于数据库操作的问号String
     * @return：比如"?,?"
     */
    private String getInsertQuestionMark() {
    	int num = DB_PROPERTIES.length + getProperties().length;
    	StringBuilder builder = new StringBuilder();
		for(int i = 0; i < num-1; i++) {
			builder.append('?').append(',');
		}
		builder.append('?');
		return builder.toString();
    }
    
    
}
