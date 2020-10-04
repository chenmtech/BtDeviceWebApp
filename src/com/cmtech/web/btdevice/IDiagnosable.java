package com.cmtech.web.btdevice;

public interface IDiagnosable {
    int CODE_REPORT_SUCCESS = 0;
    int CODE_REPORT_FAILURE = 1;
    int CODE_REPORT_ADD_NEW = 2;
    int CODE_REPORT_PROCESSING = 3;
    int CODE_REPORT_REQUEST_AGAIN = 4;
    int CODE_REPORT_NO_NEW = 5;
    
	int requestDiagnose(); // 请求诊断
	boolean applyProcessingDiagnose(); // 申请处理诊断
	int retrieveDiagnoseResult(); // 获取诊断结果
	boolean updateDiagnoseResult(long reportTime, String content); // 更新诊断结果
}
