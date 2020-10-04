package com.cmtech.web.btdevice;

public class RecordFactory {	
	private RecordFactory() {
		
	}
	
	public static BasicRecord create(RecordType type, long createTime, String devAddress) {
		switch(type) {
		case ECG:
			return new BleEcgRecord10(createTime, devAddress);
		case HR:
			return new BleHrRecord10(createTime, devAddress);		
		case THERMO:
			return new BleThermoRecord10(createTime, devAddress);
		case TH:
			return null;
		case EEG:
			return new BleEegRecord10(createTime, devAddress);
		default:
			break;
		}
		return null;
	}

}
