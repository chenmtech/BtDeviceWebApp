package com.cmtech.web.btdevice;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEcgRecord10 extends BasicRecord implements IDiagnosable{
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private String ecgData; // ecg data
    private BleEcgReport10 report = new BleEcgReport10(); // ecg diagnose report

    public BleEcgRecord10(long createTime, String devAddress) {
    	super(RecordType.ECG, createTime, devAddress);
    }
    
    public int getSampleRate() {
		return sampleRate;
	}

	public int getCaliValue() {
		return caliValue;
	}

	public int getLeadTypeCode() {
		return leadTypeCode;
	}

	public String getEcgData() {
		return ecgData;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setCaliValue(int caliValue) {
		this.caliValue = caliValue;
	}

	public void setLeadTypeCode(int leadTypeCode) {
		this.leadTypeCode = leadTypeCode;
	}

	public void setEcgData(String ecgData) {
		this.ecgData = ecgData;
	}
	
	public BleEcgReport10 getReport() {
		return report;
	}
	
    @Override
	public void fromJson(JSONObject jsonObject) {
		super.fromJson(jsonObject);
		
		sampleRate = jsonObject.getInt("sampleRate");
		caliValue = jsonObject.getInt("caliValue");
		leadTypeCode = jsonObject.getInt("leadTypeCode");
		ecgData = jsonObject.getString("ecgData");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("caliValue", caliValue);
		json.put("leadTypeCode", leadTypeCode);
		json.put("ecgData", ecgData);
		return json;
	}

	@Override
	public boolean retrieve() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select creatorPlat, creatorId, note, sampleRate, caliValue, leadTypeCode, recordSecond, ecgData from EcgRecord where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getDevAddress());
			ps.setLong(2, getCreateTime());
			rs = ps.executeQuery();
			if(rs.next()) {
				setCreator(new Account(rs.getString("creatorPlat"), rs.getString("creatorId")));
				setNote(rs.getString("note"));
				setRecordSecond(rs.getInt("recordSecond"));
				sampleRate = rs.getInt("sampleRate");
				caliValue = rs.getInt("caliValue");
				leadTypeCode = rs.getInt("leadTypeCode");
				ecgData = rs.getString("ecgData");
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
		int id = getId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into EcgRecord (ver, createTime, devAddress, creatorPlat, creatorId, note, sampleRate, caliValue, leadTypeCode, recordSecond, ecgData) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getVer());
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorId());
			ps.setString(6, getNote());
			ps.setInt(7, getSampleRate());
			ps.setInt(8, getCaliValue());
			ps.setInt(9, getLeadTypeCode());
			ps.setInt(10, getRecordSecond());
			ps.setString(11, getEcgData());
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
	public int requestReport() {
		int recordId = getId();
		if(recordId == INVALID_ID) return IDiagnosable.CODE_REPORT_FAILURE;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return IDiagnosable.CODE_REPORT_FAILURE;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String selectSql = "select ecgReportId, status from EcgReport where recordId = ?";
		String insertSql = "insert into EcgReport (status, recordId) values (?, ?)";
		String updateSql = "update EcgReport set status = ? where ecgReportId = ?";
		try {			
			ps = conn.prepareStatement(selectSql);
			ps.setInt(1, recordId);
			rs = ps.executeQuery();
			if(rs.next()) {
				int status = rs.getInt("status");
				int ecgReportId = rs.getInt("ecgReportId");
				if(status == BleEcgReport10.DONE) {
					DbUtil.closeSTMT(ps);
					ps = conn.prepareStatement(updateSql);
					ps.setInt(1, BleEcgReport10.REQUEST);
					ps.setInt(2, ecgReportId);
					if(ps.executeUpdate() != 0)
						return IDiagnosable.CODE_REPORT_REQUEST_AGAIN;
				} else {
					return IDiagnosable.CODE_REPORT_PROCESSING;
				}
			} else {
				DbUtil.closeSTMT(ps);
				ps = conn.prepareStatement(insertSql);
				ps.setInt(1, BleEcgReport10.REQUEST);
				ps.setInt(2, recordId);
				if(ps.executeUpdate() != 0)
					return IDiagnosable.CODE_REPORT_ADD_NEW;				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return IDiagnosable.CODE_REPORT_FAILURE;	
	}

	@Override
	public int retrieveReport() {
		Connection conn = DbUtil.connect();
		if(conn == null) return IDiagnosable.CODE_REPORT_FAILURE;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String selectSql = "select reportVer, reportTime, content, status from EcgReport, EcgRecord "
				+ "where EcgRecord.createTime = ? and EcgRecord.devAddress = ? and EcgRecord.id = EcgReport.recordId";
		try {
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, getCreateTime());
			ps.setString(2,  getDevAddress());
			rs = ps.executeQuery();
			if(rs.next()) {
				String reportVer = rs.getString("reportVer");
				long reportTime = rs.getLong("reportTime");
				String content = rs.getString("content");
				int status = rs.getInt("status");
				report.setVer(reportVer);
				report.setReportTime(reportTime);
				report.setContent(content);
				report.setStatus(status);
				return IDiagnosable.CODE_REPORT_SUCCESS;
			} else {
				return IDiagnosable.CODE_REPORT_NO_NEW;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return IDiagnosable.CODE_REPORT_FAILURE;	
	}

	@Override
	public boolean updateReport() {		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport, EcgRecord set reportTime = ?, content = ?, status = ? " 
				+ "where EcgRecord.createTime = ? and EcgRecord.devAddress = ? and EcgRecord.id = EcgReport.recordId and EcgReport.status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, report.getReportTime());
			ps.setString(2, report.getContent());
			ps.setInt(3, BleEcgReport10.DONE);
			ps.setLong(4, getCreateTime());
			ps.setString(5,  getDevAddress());
			ps.setInt(6, BleEcgReport10.PROCESS);
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
	
	@Override
	public boolean applyProcessingRequest() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update EcgReport, EcgRecord set status = ? " 
				+ "where EcgRecord.createTime = ? and EcgRecord.devAddress = ? and EcgRecord.id = EcgReport.recordId and EcgReport.status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, BleEcgReport10.PROCESS);
			ps.setLong(2, getCreateTime());
			ps.setString(3,  getDevAddress());
			ps.setInt(4, BleEcgReport10.REQUEST);
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
}
