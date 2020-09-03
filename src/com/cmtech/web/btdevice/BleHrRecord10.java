package com.cmtech.web.btdevice;


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
	
	
}
