package com.cmtech.web.btdevice;

public interface IDiagnosable {
    int CODE_REPORT_SUCCESS = 0;
    int CODE_REPORT_FAILURE = 1;
    int CODE_REPORT_ADD_NEW = 2;
    int CODE_REPORT_PROCESSING = 3;
    int CODE_REPORT_REQUEST_AGAIN = 4;
    int CODE_REPORT_NO_NEW = 5;
    
	int requestReport(); // 请求诊断
	boolean applyProcessingRequest(); // 申请处理请求
	int retrieveReport(); // 从数据库获取诊断信息
	boolean updateReport(); // 更新数据库中的诊断信息
}
