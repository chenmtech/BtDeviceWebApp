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

    @Override
    public String getVer() {
    	return ver;
    }
    @Override
    public void setVer(String ver) {
        this.ver = ver;
    }
    @Override
    public long getCreateTime() {
        return createTime;
    }
    @Override
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    @Override
    public String getDevAddress() {
        return devAddress;
    }
    @Override
    public void setDevAddress(String devAddress) {
        this.devAddress = devAddress;
    }
    @Override
    public String getRecordName() {
        return createTime + devAddress;
    }
    @Override
    public String getCreatorPlat() {
        return creatorPlat;
    }
    @Override
    public String getCreatorPlatId() {
    	return creatorId;
    }
    @Override
    public void setCreator(Account creator) {
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }
    @Override
    public String getNote() {
    	return note;
    }
    @Override
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
