package com.cmtech.web.dbUtil;

import org.json.JSONObject;

import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.btdevice.RecordFactory;
import com.cmtech.web.btdevice.RecordType;

public class ReportDbUtil {
    public static final int CODE_REPORT_SUCCESS = 0;
    public static final int CODE_REPORT_FAILURE = 1;
    public static final int CODE_REPORT_ADD_NEW = 2;
    public static final int CODE_REPORT_PROCESSING = 3;
    public static final int CODE_REPORT_REQUEST_AGAIN = 4;
    public static final int CODE_REPORT_NO_NEW = 5;
	
	public static JSONObject requestReport(long createTime, String devAddress) {
		BleEcgRecord10 record = (BleEcgRecord10)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		int reportCode = record.requestReport();
		JSONObject reportResult = new JSONObject();
		reportResult.put("reportCode", reportCode);
		return reportResult;
	}
	
	public static JSONObject downloadReport(long createTime, String devAddress) {
		BleEcgRecord10 record = (BleEcgRecord10)RecordFactory.create(RecordType.ECG, createTime, devAddress);
		int reportCode = record.updateReportFromDb();
		JSONObject reportResult = new JSONObject();
		reportResult.put("reportCode", reportCode);
		if(reportCode == CODE_REPORT_SUCCESS)
			reportResult.put("report", record.getReport().toJson());
		return reportResult;
	}
	
}
