package com.cmtech.web.btdevice;

public interface IRecord {
	String getVer();
	void setVer(String ver);
	RecordType getType();
    long getCreateTime();
    void setCreateTime(long createTime);
    String getDevAddress();
    void setDevAddress(String devAddress);
    String getRecordName() ;
    String getCreatorPlat();
    String getCreatorPlatId();
    void setCreator(Account creator);
    String getNote();
    void setNote(String note);
}
