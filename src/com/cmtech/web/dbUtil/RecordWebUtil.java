package com.cmtech.web.dbUtil;

import static com.cmtech.web.MyConstant.INVALID_ID;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.BasicRecord;
import com.cmtech.web.btdevice.BleEcgRecord;
import com.cmtech.web.btdevice.RecordFactory;
import com.cmtech.web.btdevice.RecordType;

/**
 * 用于执行记录相关的网络操作类
 * @author gdmc
 *
 */
public class RecordWebUtil {	
	/**
	 * 获取记录的ID号
	 * @param type：记录类型
	 * @param createTime：记录创建时间
	 * @param devAddress：记录设备
	 * @return 记录在数据表中的ID号
	 */
	public static int getId(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return INVALID_ID;
		return record.getId();
	}
	
	/**
	 * 上传一条记录，如果记录存在，则更新；如果记录不存在，则插入
	 * @param type：记录类型
	 * @param json：包含记录属性值的JSON Obj
	 * @return
	 */
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
	
	/**
	 * 下载一条记录，将其属性值打包成JSON Obj
	 * @param type
	 * @param createTime
	 * @param devAddress
	 * @return: 记录属性值打包后的JSON Obj
	 */
	public static JSONObject download(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return null;
		
		if(!record.retrieve()) return null;
		return record.toJson();
	}
	
	/**
	 * 下载满足条件的记录，将其打包为JSON Array
	 * @param types:记录类型
	 * @param creatorId：创建者ID
	 * @param fromTime: 起始采集时间
	 * @param filterStr：过滤字符串
	 * @param num：记录数
	 * @return：记录打包为JSON Array
	 */
	public static JSONArray download(RecordType[] types, int creatorId, long fromTime, String filterStr, int num) {
		if(num <= 0) return null;
		List<BasicRecord> found = BasicRecord.retrieveRecords(types, creatorId, fromTime, filterStr, num);
		if(found == null || found.isEmpty()) return null;
		
		JSONArray jsonArray = new JSONArray();
		for(BasicRecord record : found) {
			jsonArray.put(record.toJson());
		}
		
		return jsonArray;
	}	
	
	/**
	 * 删除一条记录
	 * @param type
	 * @param createTime
	 * @param devAddress
	 * @return
	 */
	public static boolean delete(RecordType type, long createTime, String devAddress) {
		BasicRecord record = RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;
		return record.delete();
	}
	
	/**
	 * 申请对记录进行诊断，记录是否需要诊断，依据当前记录的诊断报告版本号与新的诊断报告版本号的比较
	 * 如果有记录需要诊断，则将其属性值打包为JSON Obj
	 * 目前仅支持心电记录
	 * @param newReportVer：新的诊断版本号
	 * @return：
	 */
	public static JSONObject applyForDiagnose(String newReportVer) {
		BleEcgRecord record = BleEcgRecord.getFirstNeedDiagnoseRecord(newReportVer);
		if(record != null && record.applyForDiagnose() && record.retrieve()) {
			return record.toJson();
		}
		return null;
	}
	
	/**
	 * 获取记录的诊断报告，并将其属性值打包为JSON Obj.
	 * 目前仅支持心电诊断报告
	 * @param type
	 * @param createTime
	 * @param devAddress
	 * @return
	 */
	public static JSONObject retrieveDiagnoseReport(RecordType type, long createTime, String devAddress) {
		BleEcgRecord record = (BleEcgRecord)RecordFactory.create(type, createTime, devAddress);
		if(record == null || !record.retrieve()) return null;
		return record.retrieveDiagnoseReport();
	}
	
	/**
	 * 更新记录的诊断报告
	 * 目前仅支持心电记录
	 * @param type
	 * @param createTime
	 * @param devAddress
	 * @param reportVer
	 * @param reportProvider
	 * @param reportTime
	 * @param content
	 * @return
	 */
	public static boolean updateDiagnoseReport(RecordType type, long createTime, String devAddress, String reportVer, String reportProvider, long reportTime, String content) {
		BleEcgRecord record = (BleEcgRecord)RecordFactory.create(type, createTime, devAddress);
		if(record == null) return false;		
		return record.updateDiagnoseReport(reportVer, reportProvider, reportTime, content);
	}
}
