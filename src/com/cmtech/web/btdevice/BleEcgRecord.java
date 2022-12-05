package com.cmtech.web.btdevice;


import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.util.DbUtil;

/**
 * 心电记录类
 * @author gdmc
 *
 */
public class BleEcgRecord extends BasicRecord implements IDiagnosable{
	// 心电记录中要进行数据库读写的属性字段名数组
	private static final String[] PROPERTIES = {"leadTypeCode", "aveHr",  
			"segPoses", "segTimes", "annPoses", "annSymbols", "annContents"};
    
    // 导联类型
    private int leadTypeCode; // lead type code
    
    // 心电采集时断点的数据位置字符串
    private String segPoses;
    
    // 心电采集时断点的时刻点
    private String segTimes;
    
    // 心律异常条目起始时间列表字符串
    private String annPoses;
    
    // 心律异常条目标签列表字符串
    private String annSymbols;
    
    private String annContents;
    
    // 平均心率：次/分钟
    private int aveHr = 0; // average hr


    public BleEcgRecord(int accountId, long createTime, String devAddress) {
    	super(RecordType.ECG, accountId, createTime, devAddress);
    }

	public int getLeadTypeCode() {
		return leadTypeCode;
	}

	public void setLeadTypeCode(int leadTypeCode) {
		this.leadTypeCode = leadTypeCode;
	}
	
	// 获取该记录中包含的要进行数据库操作的属性字段名数组
	@Override
    public String[] getProperties() {    	
		return PROPERTIES;
    }
	
	@Override
	public File getSigFilePath() {
		return new File(getSigFileRootPath(), "ECG");
	}	
	
    @Override
	public void fromJson(JSONObject json) {
		super.fromJson(json);	
		leadTypeCode = json.getInt("leadTypeCode");
		aveHr = json.getInt("aveHr");
		segPoses = json.getString("segPoses");
		segTimes = json.getString("segTimes");
		annPoses = json.getString("annPoses");
		annSymbols = json.getString("annSymbols");
		annContents = json.getString("annContents");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("leadTypeCode", leadTypeCode);
		json.put("aveHr", aveHr);
		json.put("segPoses", segPoses);
		json.put("segTimes", segTimes);
		json.put("annPoses", annPoses);
		json.put("annSymbols", annSymbols);
		json.put("annContents", annContents);
		return json;
	}
	
	@Override
	public void readPropertiesFromResultSet(ResultSet rs) throws SQLException {
		super.readPropertiesFromResultSet(rs);
		leadTypeCode = rs.getInt("leadTypeCode");
		aveHr = rs.getInt("aveHr");
		segPoses = rs.getString("segPoses");
		segTimes = rs.getString("segTimes");
		annPoses = rs.getString("annPoses");
		annSymbols = rs.getString("annSymbols");
		annContents = rs.getString("annContents");
	}
	
	@Override
	public int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = super.writePropertiesToPreparedStatement(ps);
		ps.setInt(begin++, leadTypeCode);
		ps.setInt(begin++, aveHr);
		ps.setString(begin++, segPoses);
		ps.setString(begin++, segTimes);
		ps.setString(begin++, annPoses);
		ps.setString(begin++, annSymbols);
		ps.setString(begin++, annContents);
		return begin;
	}
	
	@Override
	public JSONObject retrieveDiagnoseReport() {
		JSONObject reportJson = new JSONObject();
		reportJson.put("reportVer", getReportVer());
		reportJson.put("reportProvider", getReportProvider());
		reportJson.put("reportTime", getReportTime());
		reportJson.put("reportContent", getReportContent());
		reportJson.put("reportStatus", getReportStatus());
		reportJson.put("aveHr", aveHr);
		return reportJson;		
	}
	
	@Override
	public boolean applyForDiagnose() {
		boolean rlt = updateReportStatus(RecordType.ECG, REPORT_STATUS_DONE, REPORT_STATUS_PROCESSING);
		if(rlt) {
			setReportStatus(REPORT_STATUS_PROCESSING);
		}
		return rlt;
	}
	
	@Override
	public boolean updateDiagnoseReport(String reportVer, String reportProvider, long reportTime, String reportContent) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgRecord set reportVer = ?, reportProvider = ?, reportTime = ?, reportContent = ?, reportStatus = ? " 
				+ "where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			int begin = 1;
			ps.setString(begin++, reportVer);
			ps.setString(begin++, reportProvider);
			ps.setLong(begin++, reportTime);
			ps.setString(begin++, reportContent);
			ps.setInt(begin++, REPORT_STATUS_DONE);
			ps.setLong(begin++, getCreateTime());
			ps.setString(begin++, getDevAddress());
			if(ps.executeUpdate() != 0) {
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
	 * 获取第一个需要诊断的心电记录
	 * 是否需要诊断，依据记录的报告状态reportStatus是否为DONE，以及reportVer是否比newReportVer小
	 * 排序按照ID，最小的ID最先诊断
	 * @param newReportVer：心电诊断报告版本
	 * @return
	 */
	public static BleEcgRecord getFirstNeedDiagnoseRecord(String newReportVer) {
		String tableName = RecordType.ECG.getTableName();
		
		Connection conn = DbUtil.connect();
		if(conn == null) return null;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select accountId, createTime, devAddress from " + tableName + 
				" where reportStatus = ? and reportVer < ? order by id limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, REPORT_STATUS_DONE);
			ps.setString(2, newReportVer);
			rs = ps.executeQuery();
			if(rs.next()) {
				int accountId = rs.getInt("accountId");
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				return new BleEcgRecord(accountId, createTime, devAddress);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
}
