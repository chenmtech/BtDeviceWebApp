package com.cmtech.web.btdevice;

public enum ReturnCode {
	SUCCESS(0),
	WEB_ERR(1), // never used in the web node
	INVALID_PARA_ERR(2),
	SIGNUP_ERR(3),
	LOGIN_ERR(4),
	ACCOUNT_ERR(5),
	UPDATE_ERR(6),
	UPLOAD_ERR(7),
	DOWNLOAD_ERR(8),
	DELETE_ERR(9),
	DATA_ERR(10),
	CHANGE_PASSWORD_ERR(11),
	SHARE_ERR(12);
	
	private final int code;
	
	private ReturnCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
