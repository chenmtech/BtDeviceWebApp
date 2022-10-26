package com.cmtech.web.btdevice;

public class RecordFactory {	
	private RecordFactory() {
		
	}
	
	public static BasicRecord create(RecordType type, int accountId, long createTime, String devAddress) {
		switch(type) {
		case ECG:
			return new BleEcgRecord(accountId, createTime, devAddress);
		case HR:
			return new BleHrRecord(accountId, createTime, devAddress);		
		case THERMO:
			return new BleThermoRecord(accountId, createTime, devAddress);
		case TH:
			return null;
		case EEG:
			return new BleEegRecord(accountId, createTime, devAddress);
		case PPG:
			return new BlePpgRecord(accountId, createTime, devAddress);
		case PTT:
			return new BlePttRecord(accountId, createTime, devAddress);
		default:
			break;
		}
		return null;
	}

}
