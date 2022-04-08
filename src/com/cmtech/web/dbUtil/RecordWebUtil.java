package com.cmtech.web.dbUtil;

import static com.cmtech.web.MyConstant.INVALID_ID;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.BasicRecord;
import com.cmtech.web.btdevice.BleEcgRecord;
import com.cmtech.web.btdevice.RecordFactory;
import com.cmtech.web.btdevice.RecordType;

public class RecordWebUtil {	
	// QUERY ID
	public static int getId(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return INVALID_ID;
		return record.getId();
	}
	
	// UPLOAD
	// If the record do not exist, then do uploading operation
	// otherwise, do updating operation
	public static boolean upload(RecordType type, JSONObject json) {
		long createTime = json.getLong("createTime");
		String devAddress = json.getString("devAddress");
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		record.fromJson(json);
		if(record.getId() == INVALID_ID)
			return record.insert();
		else
			return record.update();
	}
	
	// DOWNLOAD
	public static JSONObject download(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return null;
		
		if(!record.retrieve()) return null;
		return record.toJson();
	}
	
	// DELETE
	public static boolean delete(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		return record.delete();
	}
	
	// DOWNLOAD RECORD LIST
	// who: creatorPlat+creatorId
	// when: later than fromTime
	// include: noteSearchStr
	// howmuch: num
	public static JSONArray downloadList(RecordType[] types, int creatorId, long fromTime, String noteSearchStr, int num) {
		if(num <= 0) return null;
		List<BasicRecord> found = BasicRecord.findRecords(types, creatorId, fromTime, noteSearchStr, num);
		if(found == null || found.isEmpty()) return null;
		
		JSONArray jsonArray = new JSONArray();
		for(BasicRecord record : found) {
			jsonArray.put(record.basicToJson());
		}
		
		return jsonArray;
	}
	
	// REQUEST ECG DIAGNOSE AND GET REPORT IF READY
	public static JSONObject requestDiagnoseReport(long createTime, String devAddress) {
		BleEcgRecord record = (BleEcgRecord)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		if(record == null || !record.retrieve()) return null;
		return record.retrieveDiagnose();
	}
	
	// APPLY FOR DIAGNOSE A ECGRECORD
	// Return the json object of the record if the diagnose request of a record exists
	public static JSONObject applyForDiagnose() {
		BleEcgRecord record = BleEcgRecord.getFirstRequestRecord();
		if(record != null && record.applyForDiagnose() && record.retrieve()) {
			return record.toJson();
		}
		return null;
	}
	
	// UPDATE DIAGNOSE
	public static boolean updateDiagnose(long createTime, String devAddress, String reportVer, long reportTime, String content) {
		BleEcgRecord record = (BleEcgRecord)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		if(record == null) return false;		
		return record.updateDiagnose(reportVer, reportTime, content);
	}
}
