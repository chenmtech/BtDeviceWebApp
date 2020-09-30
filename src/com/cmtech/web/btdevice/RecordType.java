package com.cmtech.web.btdevice;

public enum RecordType {
	ALL(0, "所有", ""), 
	ECG(1, "心电", "EcgRecord"), 
	HR(2, "心率", "HrRecord"), 
	THERMO(3, "体温", "ThermoRecord"), 
	TH(4, "温湿度", ""), 
	EEG(5, "脑电", "EegRecord");  

    private final int code;  
    private final String name;  
    private final String tableName;

    private RecordType(int code, String name, String tableName) {  
        this.code = code;  
        this.name = name;  
        this.tableName = tableName;
    }  
    
    public static RecordType fromCode(int code) {  
        for (RecordType type : RecordType.values()) {  
            if (type.getCode() == code) {  
                return type;  
            }  
        }  
        return null;  
    }  

    public static String getName(int code) {  
    	RecordType type = RecordType.fromCode(code);
    	return (type == null) ? null : type.name;
    }  

    public String getName() {  
        return name;  
    } 
    
    public int getCode() {  
        return code;  
    }  

	public String getTableName() {
		return tableName;
	}

	@Override
	public String toString() {
		return getName();
	} 
}
