package com.cmtech.web.btdevice;

import com.cmtech.web.dbop.Account;

public abstract class AbstractRecord implements IRecord{
    private byte[] ver = new byte[2]; // record version
    private long createTime; //
    private String devAddress; //
    private String creatorPlat;
    private String creatorId;

    protected AbstractRecord() {
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
    }
    
    public byte[] getVer() {
    	return ver;
    }
    
    public void setVer(byte[] ver) {
        this.ver[0] = ver[0];
        this.ver[1] = ver[1];
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

    @Override
    public String toString() {
        return createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId;
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
