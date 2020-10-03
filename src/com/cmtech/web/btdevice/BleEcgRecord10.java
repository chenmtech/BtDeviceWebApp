package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.*;

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
	
	public JSONObject getReportJson() {
		return report.toJson();
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
	public int requestDiagnose() {
		if(!report.retrieve()) {
			report.setStatus(BleEcgReport10.REQUEST);
			if(report.insert())
				return IDiagnosable.CODE_REPORT_ADD_NEW;
		} else {
			report.setStatus(BleEcgReport10.REQUEST);
			if(report.updateStatusIfBeing(BleEcgReport10.DONE)) {
				return IDiagnosable.CODE_REPORT_REQUEST_AGAIN;
			} else {
				return IDiagnosable.CODE_REPORT_PROCESSING;
			}
		}
		return IDiagnosable.CODE_REPORT_FAILURE;
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
