package com.cmtech.web.btdevice;

public abstract class AbstractRecord implements IRecord{
    private String ver; // record version
    private long createTime; //
    private String devAddress; //
    private String creatorPlat;
    private String creatorId;
    private String note;

    protected AbstractRecord() {
    	ver = "";
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
        note = "";
    }
    
    public String getVer() {
    	return ver;
    }
    
    public void setVer(String ver) {
        this.ver = ver;
    }
    @Override
    public long getCreateTime() {
        return createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    @Override
    public String getDevAddress() {
        return devAddress;
    }
    public void setDevAddress(String devAddress) {
        this.devAddress = devAddress;
    }
    @Override
    public String getRecordName() {
        return createTime + devAddress;
    }
    public String getCreatorPlat() {
        return creatorPlat;
    }
    public String getCreatorPlatId() {
    	return creatorId;
    }
    public void setCreator(Account creator) {
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }
    public String getNote() {
    	return note;
    }
    public void setNote(String note) {
    	this.note = note;
    }

    @Override
    public String toString() {
        return createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + note;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        IRecord other = (IRecord) otherObject;
        return getRecordName().equals(other.getRecordName());
    }

    @Override
    public int hashCode() {
        return getRecordName().hashCode();
    }
}
