package com.cmtech.web.dbUtil;

import static com.cmtech.web.MyConstant.INVALID_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.BasicRecord;
import com.cmtech.web.btdevice.BasicRecord.TmpRecord;
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
		
		List<RecordType> types = new ArrayList<>();
		if(type == RecordType.ALL) {
			for(RecordType t : RecordType.values()) {
				if(t != RecordType.ALL && t != RecordType.TH) {
					types.add(t);
				}
			}
		}
		else
			types.add(type);
		
		JSONArray jsonArray = new JSONArray();
		List<TmpRecord> found = new ArrayList<>();
		for(RecordType t : types) {
			found.addAll(BasicRecord.searchRecord(t, creatorPlat, creatorId, fromTime, noteSearchStr, num));
		}
		Collections.sort(found, new Comparator<TmpRecord>() {
		    @Override
		    public int compare(TmpRecord o1, TmpRecord o2) {
		    	int rlt = 0;
		        if(o2.getCreateTime() > o1.getCreateTime()) rlt = 1;
		        else if(o2.getCreateTime() < o1.getCreateTime()) rlt = -1;
		        return rlt;
		    }
		});

		int N = Math.min(num, found.size());
		for(int i = 0; i < N; i++) {
			jsonArray.put(i, downloadBasicInfo(found.get(i).getType(), found.get(i).getCreateTime(), found.get(i).getDevAddress()));
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
		if(record != null && record.applyForDiagnose() && record.retrieve()) {
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
	
	private static JSONObject downloadBasicInfo(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return null;
		
		if(!record.retrieveBasicInfo()) return null;
		JSONObject json = new JSONObject();
		json.put("recordTypeCode", type.getCode());
		json.put("createTime", createTime);
		json.put("devAddress", devAddress);
		json.put("ver", record.getVer());
		json.put("creatorPlat", record.getCreatorPlat());
		json.put("creatorId", record.getCreatorId());
		json.put("recordSecond", record.getRecordSecond());
		json.put("note", record.getNote());
	
		return json;
	}
}
