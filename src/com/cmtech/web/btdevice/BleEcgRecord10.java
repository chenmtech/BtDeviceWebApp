package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public class BleEcgRecord10 extends BasicRecord implements IDiagnosable{
	private static final String SELECT_STR = BasicRecord.SELECT_STR + "sampleRate, caliValue, leadTypeCode, ecgData";
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private String ecgData; // ecg data
    private BleEcgReport10 report; // ecg diagnose report

    public BleEcgRecord10(long createTime, String devAddress) {
    	super(RecordType.ECG, createTime, devAddress);
    	report = new BleEcgReport10(createTime, devAddress);
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
    
    public String getSelectStr() {
		return SELECT_STR;
	}
	
    @Override
	public void fromJson(JSONObject json) {
		super.fromJson(json);		
		sampleRate = json.getInt("sampleRate");
		caliValue = json.getInt("caliValue");
		leadTypeCode = json.getInt("leadTypeCode");
		ecgData = json.getString("ecgData");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject json = super.toJson();
		json.put("sampleRate", sampleRate);
		json.put("caliValue", caliValue);
		json.put("leadTypeCode", leadTypeCode);
		json.put("ecgData", ecgData);
		json.put("report", report.toJson());
		return json;
	}
	
	public JSONObject getReportJson() {
		return report.toJson();
	}
	
	@Override
	public void setFromResultSet(ResultSet rs) throws SQLException {
		super.setFromResultSet(rs);
		sampleRate = rs.getInt("sampleRate");
		caliValue = rs.getInt("caliValue");
		leadTypeCode = rs.getInt("leadTypeCode");
		ecgData = rs.getString("ecgData");
	}

	@Override
	public boolean retrieve() {
		report.retrieve();
		return super.retrieve();
	}

	@Override
	public boolean delete() {
		report.delete();
		return super.delete();
	}

	@Override
	public boolean insert() {
		int id = getId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into EcgRecord (ver, createTime, devAddress, creatorPlat, creatorId, note, recordSecond, sampleRate, caliValue, leadTypeCode, ecgData) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getVer());
			ps.setLong(2, getCreateTime());
			ps.setString(3, getDevAddress());
			ps.setString(4, getCreatorPlat());
			ps.setString(5, getCreatorId());
			ps.setString(6, getNote());
			ps.setInt(7, getRecordSecond());
			ps.setInt(8, sampleRate);
			ps.setInt(9, caliValue);
			ps.setInt(10, leadTypeCode);
			ps.setString(11, ecgData);
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
	public int requestDiagnose() {
		if(!report.retrieve()) {
			report.setStatus(BleEcgReport10.REQUEST);
			if(report.insert())
				return IDiagnosable.CODE_REPORT_ADD_NEW;
			else
				return IDiagnosable.CODE_REPORT_FAILURE;
		} else {
			report.setStatus(BleEcgReport10.REQUEST);
			if(report.updateStatusIfBeing(BleEcgReport10.DONE)) {
				return IDiagnosable.CODE_REPORT_REQUEST_AGAIN;
			} else {
				return IDiagnosable.CODE_REPORT_PROCESSING;
			}
		}
	}

	@Override
	public int retrieveDiagnoseResult() {
		if(report.retrieve()) {
			return IDiagnosable.CODE_REPORT_SUCCESS;
		} else {
			return IDiagnosable.CODE_REPORT_NO_NEW;
		}
	}

	@Override
	public boolean updateDiagnoseResult(long reportTime, String content) {
		report.setReportTime(reportTime);
		report.setContent(content);
		report.setStatus(BleEcgReport10.DONE);
		return report.updateIfBeing(BleEcgReport10.PROCESS);
	}
	
	@Override
	public boolean applyForDiagnose() {
		report.setStatus(BleEcgReport10.PROCESS);
		return report.updateStatusIfBeing(BleEcgReport10.REQUEST);
	}
	
	public static BleEcgRecord10 getFirstRequestRecord() {
		BleEcgReport10 report = BleEcgReport10.getFirstRequestReport();
		if(report != null) {
			return new BleEcgRecord10(report.getCreateTime(), report.getDevAddress());
		}
		return null;
	}
}
