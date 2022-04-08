package com.cmtech.web.btdevice;

import org.json.JSONObject;

public interface IDiagnosable {
    int CODE_REPORT_SUCCESS = 0;
    int CODE_REPORT_FAILURE = 1;
    int CODE_REPORT_ADD_NEW = 2;
    int CODE_REPORT_PROCESSING = 3;
    int CODE_REPORT_REQUEST_AGAIN = 4;
    int CODE_REPORT_NO_NEW = 5;
    
	JSONObject retrieveDiagnose(); // 如果有新的诊断结果，则获取，如果没有，则申请
	boolean applyForDiagnose(); // 申请诊断
	boolean updateDiagnose(String reportVer, long reportTime, String content); // 更新诊断
}
