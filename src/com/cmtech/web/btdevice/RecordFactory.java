package com.cmtech.web.btdevice;

public class RecordFactory {	
	private RecordFactory() {
		
	}
	
	public static BasicRecord create(RecordType type, long createTime, String devAddress) {
		switch(type) {
		case ECG:
			return new BleEcgRecord(createTime, devAddress);
		case HR:
			return new BleHrRecord(createTime, devAddress);		
		case THERMO:
			return new BleThermoRecord(createTime, devAddress);
		case TH:
			return null;
		case EEG:
			return new BleEegRecord(createTime, devAddress);
		case PPG:
			return new BlePpgRecord(createTime, devAddress);
		default:
			break;
		}
		return null;
	}

}
