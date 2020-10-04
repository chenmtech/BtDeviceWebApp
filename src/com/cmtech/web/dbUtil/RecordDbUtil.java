package com.cmtech.web.dbUtil;

import static com.cmtech.web.MyConstant.INVALID_ID;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.BasicRecord;
import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.btdevice.IDiagnosable;
import com.cmtech.web.btdevice.RecordFactory;
import com.cmtech.web.btdevice.RecordType;

public class RecordDbUtil {
	
	// QUERY ID
	public static int getId(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return INVALID_ID;
		return record.getId();
	}
	
	// UPDATE NOTE
	public static boolean updateNote(RecordType type, long createTime, String devAddress, String note) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		record.setNote(note);
		return record.update();
	}
	
	// DELETE
	public static boolean delete(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		return record.delete();
	}
	
	// INSERT
	public static boolean insert(RecordType type, JSONObject json) {
		long createTime = json.getLong("createTime");
		String devAddress = json.getString("devAddress");
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		record.fromJson(json);
		return record.insert();
	}
	
	// DOWNLOAD
	public static JSONObject download(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return null;
		
		if(!record.retrieve()) return null;
		return record.toJson();
	}
	
	// DOWNLOAD BASIC RECORD INFO
	// who: creatorPlat+creatorId
	// when: later than fromTime
	// include: noteSearchStr
	// howmuch: num
	public static JSONArray downloadBasicInfo(RecordType type, String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num) {
		if(num <= 0) return null;
		List<BasicRecord> found = BasicRecord.findRecord(type, creatorPlat, creatorId, fromTime, noteSearchStr, num);
		if(found == null || found.isEmpty()) return null;
		
		JSONArray jsonArray = new JSONArray();
		for(int i = 0; i < found.size(); i++) {
			jsonArray.put(i, found.get(i).basicToJson());
		}
		
		return jsonArray;
	}
	
	// REQUEST DIAGNOSE
	public static JSONObject requestDiagnose(long createTime, String devAddress) {
		BleEcgRecord10 record = (BleEcgRecord10)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		int reportCode = record.requestDiagnose();
		JSONObject reportResult = new JSONObject();
		reportResult.put("reportCode", reportCode);
		return reportResult;
	}
	
	// APPLY FOR DIAGNOSE
	// Return the json object of the record if exist the request record
	public static JSONObject applyForDiagnose() {
		BleEcgRecord10 record = BleEcgRecord10.getFirstRequestRecord();
		if(record != null && record.applyProcessingDiagnose() && record.retrieve()) {
			return record.toJson();
		}
		return null;
	}
	
	// DOWNLOAD DIAGNOSE RESULT
	public static JSONObject downloadDiagnoseResult(long createTime, String devAddress) {
		BleEcgRecord10 record = (BleEcgRecord10)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		int reportCode = record.retrieveDiagnoseResult();
		JSONObject reportResult = new JSONObject();
		reportResult.put("reportCode", reportCode);
		if(reportCode == IDiagnosable.CODE_REPORT_SUCCESS)
			reportResult.put("report", record.getReportJson());
		return reportResult;
	}

	// UPLOAD DIAGNOSE RESULT
	public static boolean uploadDiagnoseResult(long createTime, String devAddress, long reportTime, String content) {
		BleEcgRecord10 record = (BleEcgRecord10)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		if(record == null) return false;		
		return record.updateDiagnoseResult(reportTime, content);
	}
}
