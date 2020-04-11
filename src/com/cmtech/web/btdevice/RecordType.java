package com.cmtech.web.btdevice;

public enum RecordType {
	ECG("心电", 1), HR("绿色", 2), THERMO("体温", 3), TH("温湿度", 4);  

    private String name;  
    private int code;  

    private RecordType(String name, int code) {  
        this.name = name;  
        this.code = code;  
    }  
    
    public static RecordType getType(int code) {  
        for (RecordType type : RecordType.values()) {  
            if (type.getCode() == code) {  
                return type;  
            }  
        }  
        return null;  
    }  

    public static String getName(int code) {  
        for (RecordType type : RecordType.values()) {  
            if (type.getCode() == code) {  
                return type.name;  
            }  
        }  
        return null;  
    }  

    public String getName() {  
        return name;  
    }  
    public void setName(String name) {  
        this.name = name;  
    }  
    public int getCode() {  
        return code;  
    }  
    public void setCode(int code) {  
        this.code = code;  
    } 
}