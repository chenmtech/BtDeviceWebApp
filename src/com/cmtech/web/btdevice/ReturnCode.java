package com.cmtech.web.btdevice;

public enum ReturnCode {
	SUCCESS(0),
	WEB_ERR(1), // never used in the web node
	INVALID_PARA_ERR(2),
	DATA_ERR(3);
	
	private final int code;
	
	private ReturnCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
