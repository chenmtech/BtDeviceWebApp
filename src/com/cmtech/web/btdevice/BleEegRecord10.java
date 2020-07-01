package com.cmtech.web.btdevice;

public class BleEegRecord10 extends AbstractRecord{
	private int sampleRate; // sample rate
    private int caliValue; // calibration value of 1mV
    private int leadTypeCode; // lead type code
    private int recordSecond; // unit: s
    private String eegData; // ecg data
    
    public BleEegRecord10() {
    	super();
    }

	public int getSampleRate() {
		return sampleRate;
	}

	public int getCaliValue() {
		return caliValue;
	}

	public int getLeadTypeCode() {
		return leadTypeCode;
	}

	public int getRecordSecond() {
		return recordSecond;
	}

	public String getEegData() {
		return eegData;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setCaliValue(int caliValue) {
		this.caliValue = caliValue;
	}

	public void setLeadTypeCode(int leadTypeCode) {
		this.leadTypeCode = leadTypeCode;
	}

	public void setRecordSecond(int recordSecond) {
		this.recordSecond = recordSecond;
	}

	public void setEegData(String eegData) {
		this.eegData = eegData;
	}
	
}
