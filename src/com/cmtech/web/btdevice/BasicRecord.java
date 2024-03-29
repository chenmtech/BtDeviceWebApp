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

import com.cmtech.web.util.DbUtil;

/**
 * 所有记录的基础类，包含各种记录共有的属性和基本操作
 * @author gdmc
 *
 */
public abstract class BasicRecord implements IRecord, IJsonable{
	//------------------------------------------------------------------------常量
	// 记录中要进行数据库读写的基本属性字段字符串数组
	private static final String[] BASIC_PROPERTIES = {
			"accountId", "createTime", "devAddress", "ver", "creatorId", "comment", 
			"sampleRate", "channelNum", "sigLen", "bytePerDatum", "gain", "unit",
			"reportVer", "reportProvider", "reportTime", "reportContent", "reportStatus"};
	
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
	// 类型
	private final RecordType type;
	
    // 版本号
    private String ver;

    // 拥有者账户ID
    private int accountId;
	
	// 创建时间
    private final long createTime;
    
    // 设备地址
    private final String devAddress;    

    // 创建者账户ID
    private int creatorId;

	// 采样率
	private int sampleRate = 0; 
    
	// 通道数
    private int channelNum;

    // 信号长度
    private int sigLen = 0;
    
	// 每个数据占用字节数
	private int bytePerDatum = 1;	
	
	// 每个通道的增益值数组构成的字符串，增益值是指信号的一个物理单位对应的ADU值
    private String gain = "1"; 
    
    // 每个通道的物理单位数组构成的字符串，比如："mV,C"代表通道1的物理量为毫伏，通道2的物理量为摄氏度
    private String unit = "unknown";
    
    // 备注
    private String comment;    
    
    // ------------------------------------------------------------诊断报告相关实例变量 
    
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
    // 信号文件存放根路径
    private static File sigFileRootPath = new File(System.getProperty("catalina.home")+File.separator + "MY_FILE");
    

    
    //-----------------------------------------------------静态函数
    
    /**
     * 获取信号文件存储的根路径File
     * @return 根路径File
     */
	public static File getSigFileRootPath() {
		return sigFileRootPath;
	}
    
    /**
     * 设置信号文件根路径
     * @param sigPath
     */
    public static void setSigFileRootPath(File rootPath) {
    	sigFileRootPath = rootPath;
    }    
    

    /**
     * 用于从数据库中查找满足条件的记录，并按创建时间排序
     * @param types：要查找的记录类型
     * @param creatorId：记录创建者ID
     * @param fromTime：记录起始创建时间
     * @param filterStr：备注中包含的字符串
     * @param num：查找的记录数
     * @return：得到的记录列表
     */
    public static List<BasicRecord> retrieveRecords(RecordType[] types, int accountId, long fromTime, String filterStr, int num) {
    	if(num <= 0) return null;
		
		List<BasicRecord> found = new ArrayList<>();
		for(RecordType t : types) {
			// 不支持ALL和TH类型的记录
			if(t == RecordType.ALL || t == RecordType.TH) {
				continue;
			}
			List<BasicRecord> tmp = BasicRecord.searchOneTypeRecordList(t, accountId, fromTime, filterStr, num);
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

    /**
     * 获取一种记录类型的记录列表，仅包含accountId, createTime和devAddress字段信息
     * @param type 类型
     * @param accountId 拥有者ID
     * @param fromTime 其实时间
     * @param filterStr 筛选字符串
     * @param num 记录数量
     * @return 记录列表
     */
	private static List<BasicRecord> searchOneTypeRecordList(RecordType type, int accountId, long fromTime, String filterStr, int num) {
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
			if(accountId != INVALID_ID) {
				where += "accountId = ? and "; 
			}
			if(!"".equals(filterStr)) {
				where += "comment REGEXP ? and ";
			}
			where += "createTime < ? order by createTime desc limit ?";
			sql += where;
			
			ps = conn.prepareStatement(sql);
			int i = 1;
			if(accountId != INVALID_ID) {
				ps.setInt(i++, accountId);
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
				found.add(RecordFactory.create(type, accountId, createTime, devAddress));
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
    BasicRecord(RecordType type, int accountId, long createTime, String devAddress) {
    	this.type = type;
    	this.accountId = accountId;
        this.createTime = createTime;
        this.devAddress = devAddress;
    	ver = DEFAULT_RECORD_VER;
        creatorId = INVALID_ID;
        comment = "";
        sigLen = 0;
        channelNum = 1;
        bytePerDatum = 2;
    }
    
    //-------------------------------------------------------实例方法
    public RecordType getType() {
		return type;
	}

    public String getVer() {
    	return ver;
    }

    public int getAccountId() {
    	return accountId;
    }
    
    public void setAccountId(int accountId) {
    	this.accountId = accountId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getDevAddress() {
        return devAddress;
    }

    /**
     * 获取记录的全局唯一标识符
     * @return
     */
    public String getRecordUuid() {
        return accountId + createTime + devAddress;
    }

    public int getCreatorId() {
    	return creatorId;
    }

    public String getComment() {
    	return comment;
    }
    
    public int getSampleRate() {
		return sampleRate;
	}
    
    public int getBytePerDatum() {
    	return bytePerDatum;
    }

	public String getGain() {
		return gain;
	}

    public int getSigLen() {
		return sigLen;
	}
    
    public int getChannelNum() {
    	return channelNum;
    }
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setGain(String gain) {
		this.gain = gain;
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
	
	public String getSigFileName() {
        return getDevAddress().replace(":", "")+getCreateTime();
    }
	
    
	@Override
    public void fromJson(JSONObject json) {
		ver = json.getString("ver");
		creatorId = json.getInt("creatorId");
		comment = json.getString("comment");	
		sampleRate = json.getInt("sampleRate");
		channelNum = json.getInt("channelNum");
		sigLen = json.getInt("sigLen");
		bytePerDatum = json.getInt("bytePerDatum");
		gain = json.getString("gain");
		unit = json.getString("unit");
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
		json.put("accountId", accountId);
		json.put("createTime", createTime);
		json.put("devAddress", devAddress);
    	json.put("ver", ver);
		json.put("creatorId", creatorId);
		json.put("comment", comment);
		json.put("sampleRate", sampleRate);
		json.put("channelNum", channelNum);
		json.put("sigLen", sigLen);
		json.put("bytePerDatum", bytePerDatum);
		json.put("gain", gain);
		json.put("unit", unit);
		json.put("reportVer", reportVer);
		json.put("reportProvider", reportProvider);
		json.put("reportTime", reportTime);
		json.put("reportContent", reportContent);
		json.put("reportStatus", reportStatus);
		return json;
	}
    

    /**
     * 从数据库操作的ResultSet中读取属性值
     * @param rs
     * @throws SQLException
     */
    @Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		ver = rs.getString("ver");
		creatorId = rs.getInt("creatorId");
		comment = rs.getString("comment");
		sampleRate = rs.getInt("sampleRate");
		channelNum = rs.getInt("channelNum");
		sigLen = rs.getInt("sigLen");
		bytePerDatum = rs.getInt("bytePerDatum");
		gain = rs.getString("gain");
		unit = rs.getString("unit");
		reportVer = rs.getString("reportVer");
		reportProvider = rs.getString("reportProvider");
		reportTime = rs.getLong("reportTime");
		reportContent = rs.getString("reportContent");
		reportStatus = rs.getInt("reportStatus");
	}	
	
	/**
	 * 将属性值写入数据库操作的PreparedStatement中
	 * 注意：写入的字段顺序必须和getPropertiesString()返回的字段顺序一致
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
    @Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = 1;
		ps.setInt(begin++, accountId);
		ps.setLong(begin++, createTime);
		ps.setString(begin++, devAddress);
		ps.setString(begin++, ver);
		ps.setInt(begin++, creatorId);
		ps.setString(begin++, comment);
		ps.setInt(begin++, sampleRate);
		ps.setInt(begin++, channelNum);
		ps.setInt(begin++, sigLen);
		ps.setInt(begin++, bytePerDatum);
		ps.setString(begin++, gain);
		ps.setString(begin++, unit);
		ps.setString(begin++, reportVer);
		ps.setString(begin++, reportProvider);
		ps.setLong(begin++, reportTime);
		ps.setString(begin++, reportContent);
		ps.setInt(begin++, reportStatus);
		return begin;
	}

	/**
	 * 获取该记录在数据表中的ID：用type, accountId, createTime和devAddress
	 */
    @Override
    public final int getId() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return INVALID_ID;
    	
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from " + tableName + " where accountId = ? and createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.setLong(2, createTime);
			ps.setString(3, devAddress);
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
    
    /**
     * 从数据库中获取该记录的属性字段值，属性应包含于getPropertiesString()的字符串中
     * 一般包括BasicRecord基类和子类中的所有属性
     */
    @Override
	public final boolean retrieve() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select " + getPropertiesString() + " from " + tableName + " where accountId = ? and createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.setLong(2, createTime);
			ps.setString(3, devAddress);
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
	 * 当该账号是记录的创建者时，同时删除记录信号文件
	 */
    @Override
	public boolean delete() {
    	boolean success = false;
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		// 先获取记录创建者ID
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select creatorId from " + tableName + " where accountId = ? and createTime = ? and devAddress = ?";
		int creatorId = INVALID_ID;
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.setLong(2, createTime);
			ps.setString(3, devAddress);
			rs = ps.executeQuery();
			if(rs.next()) {
				creatorId = rs.getInt("creatorId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, null);
		}
		
		// 删除数据库中的记录
		PreparedStatement ps1 = null;
		String sql1 = "delete from " + tableName + " where accountId = ? and createTime = ? and devAddress = ?";
		try {
			ps1 = conn.prepareStatement(sql1);
			ps1.setInt(1, accountId);
			ps1.setLong(2, createTime);
			ps1.setString(3, devAddress);
			if(ps1.executeUpdate() != 0)
				success = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps1, conn);
		}
		
		// 如果拥有者也是记录的创建者，才删除信号文件
		if(success && creatorId == accountId) {
			File file = new File(getSigFilePath(), getSigFileName());
			if(file.exists()) success = file.delete();
		}
		return success;
	}

	/**
	 * 更新记录，目前只能更新comment字段
	 */
    @Override
	public boolean update() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update " + tableName + " set comment = ? where accountId = ? and createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, comment);
			ps.setInt(2, accountId);
			ps.setLong(3, createTime);
			ps.setString(4, devAddress);
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
		String sql = "update " + tableName +" set reportStatus = ? where accountId = ? and createTime = ? and devAddress = ? and reportStatus = ?";
		try {
			int begin = 1;
			ps = conn.prepareStatement(sql);
			ps.setInt(begin++, toStatus);
			ps.setInt(begin++, accountId);
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
        return type + "-" + accountId + "-" +  createTime + "-" + devAddress + "-" + creatorId + "-" + comment;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        BasicRecord other = (BasicRecord) otherObject;
        return getRecordUuid().equals(other.getRecordUuid());
    }

    @Override
    public int hashCode() {
        return getRecordUuid().hashCode();
    }

    /**
     * 获取属性字段用于数据库操作的String
     * @return 属性字符串
     */
    private String getPropertiesString() {
    	StringBuilder builder = new StringBuilder();
    	int len = BASIC_PROPERTIES.length;
    	for(int i = 0; i < len; i++) {
    		builder.append(BASIC_PROPERTIES[i]).append(',');
    	}
    	
    	String[] properties = getProperties();
    	len = properties.length;
    	if(len > 0) {
	    	for(int i = 0; i < len; i++) {
	    		builder.append(properties[i]).append(',');
	    	}
    	}
    	builder.deleteCharAt(builder.length()-1);    	
    	return builder.toString();
    }
    
    /**
     * 获取属性字段用于数据库操作的问号String
     * @return：比如"?,?"
     */
    private String getInsertQuestionMark() {
    	int num = BASIC_PROPERTIES.length + getProperties().length;
    	StringBuilder builder = new StringBuilder();
		for(int i = 0; i < num; i++) {
			builder.append('?').append(',');
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
    }
    
    
}
