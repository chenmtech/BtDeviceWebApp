package com.cmtech.web.btdevice;

public interface IRecord {
	String getVer();
	void setVer(String ver);
	RecordType getType();
    long getCreateTime();
    String getDevAddress();
    String getRecordName() ;
    String getCreatorPlat();
    String getCreatorId();
    void setCreator(Account creator);
    String getNote();
    void setNote(String note);
}
