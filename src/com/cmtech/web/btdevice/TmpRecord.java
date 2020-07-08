package com.cmtech.web.btdevice;

public class TmpRecord {
	int id;
	long createTime;
	RecordType type;
	
	public TmpRecord(int id, long createTime, RecordType type) {
		this.id = id;
		this.createTime= createTime;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public long getCreateTime() {
		return createTime;
	}

	public RecordType getType() {
		return type;
	}
	
	
}
