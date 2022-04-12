package com.cmtech.web.btdevice;

import org.json.JSONObject;

/**
 * 可诊断类接口
 * @author gdmc
 *
 */
public interface IDiagnosable {
    int CODE_REPORT_SUCCESS = 0;
    int CODE_REPORT_FAILURE = 1;
    int CODE_REPORT_ADD_NEW = 2;
    int CODE_REPORT_PROCESSING = 3;
    int CODE_REPORT_REQUEST_AGAIN = 4;
    int CODE_REPORT_NO_NEW = 5;
    
    // 获取诊断报告
	JSONObject retrieveDiagnoseReport(); 
	
	// 申请诊断
	boolean applyForDiagnose(); 
	
	// 更新诊断报告
	boolean updateDiagnoseReport(String reportVer, long reportTime, String content); 
}
