package com.cmtech.web.btdevice;

import org.json.JSONObject;

public class BleThermoRecord10 extends AbstractRecord {
    private String temp;
    
    public BleThermoRecord10() {
    	this(0, "");
    }
    
    public BleThermoRecord10(long createTime, String devAddress) {
    	super(RecordType.THERMO, createTime, devAddress);
    }
    
	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}
    
	public static BleThermoRecord10 createFromJson(JSONObject jsonObject) {
		String ver = jsonObject.getString("ver");
		long createTime = jsonObject.getLong("createTime");
		String devAddress = jsonObject.getString("devAddress");
		String creatorPlat = jsonObject.getString("creatorPlat");
		String creatorId = jsonObject.getString("creatorId");
		String note = jsonObject.getString("note");
		String temp = jsonObject.getString("temp");
		
		BleThermoRecord10 record = new BleThermoRecord10();
		if("".equals(ver)) {
			ver = "1.0";
		}
		record.setVer(ver);
		record.setCreateTime(createTime);
		record.setDevAddress(devAddress);
		record.setCreator(new Account(creatorPlat, creatorId));
		record.setNote(note);
		record.setTemp(temp);
		return record;
	}
}
