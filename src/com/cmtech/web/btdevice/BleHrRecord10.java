package com.cmtech.web.btdevice;

import org.json.JSONObject;

public class BleHrRecord10 extends AbstractRecord {
	private String hrList; // list of the filtered HR
    private short hrMax;
    private short hrAve;
    private String hrHist; // HR histogram value
    private int recordSecond; // unit: s
	
	public BleHrRecord10() {
    	super();
    }

	public String getHrList() {
		return hrList;
	}

	public void setHrList(String hrList) {
		this.hrList = hrList;
	}

	public short getHrMax() {
		return hrMax;
	}

	public void setHrMax(short hrMax) {
		this.hrMax = hrMax;
	}

	public short getHrAve() {
		return hrAve;
	}

	public void setHrAve(short hrAve) {
		this.hrAve = hrAve;
	}

	public String getHrHist() {
		return hrHist;
	}

	public void setHrHist(String hrHist) {
		this.hrHist = hrHist;
	}

	public int getRecordSecond() {
		return recordSecond;
	}

	public void setRecordSecond(int recordSecond) {
		this.recordSecond = recordSecond;
	}
	
	public static BleHrRecord10 createFromJson(JSONObject jsonObject) {
		String ver = jsonObject.getString("ver");
		long createTime = jsonObject.getLong("createTime");
		String devAddress = jsonObject.getString("devAddress");
		String creatorPlat = jsonObject.getString("creatorPlat");
		String creatorId = jsonObject.getString("creatorId");
		String note = jsonObject.getString("note");
		String hrList = jsonObject.getString("hrList");
		short hrMax = (short) jsonObject.getInt("hrMax");
		short hrAve = (short) jsonObject.getInt("hrAve");
		String hrHist = jsonObject.getString("hrHist");
		int recordSecond = jsonObject.getInt("recordSecond");
		
		BleHrRecord10 record = new BleHrRecord10();
		if("".equals(ver)) {
			ver = "1.0";
		}
		record.setVer(ver);
		record.setCreateTime(createTime);
		record.setDevAddress(devAddress);
		record.setCreator(new Account(creatorPlat, creatorId));
		record.setNote(note);
		record.setHrList(hrList);
		record.setHrMax(hrMax);
		record.setHrAve(hrAve);
		record.setHrHist(hrHist);
		record.setRecordSecond(recordSecond);
		return record;
	}
}
