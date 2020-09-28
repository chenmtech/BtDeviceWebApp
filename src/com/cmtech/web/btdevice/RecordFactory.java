package com.cmtech.web.btdevice;

import org.json.JSONObject;

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
	
	public static BasicRecord createFromJson(RecordType type, JSONObject json) {
		switch(type) {
		case ECG:
			return BleEcgRecord10.createFromJson(json);
		case HR:
			return BleHrRecord10.createFromJson(json);	
		case THERMO:
			return BleThermoRecord10.createFromJson(json);
		case TH:
			return null;
		case EEG:
			return BleEegRecord10.createFromJson(json);
		default:
			break;
		}
		return null;
	}

}
