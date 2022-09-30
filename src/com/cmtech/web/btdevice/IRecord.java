package com.cmtech.web.btdevice;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IRecord extends IDbOperation{
	// 获取记录中要进行数据库操作的属性字段字符串数组
	String[] getProperties();
    
	// 获取信号文件存储路径
	File getSigFilePath();
	
	// 从数据库操作的ResultSet中读取属性值赋值给记录属性
	void readPropertiesFromResultSet(ResultSet rs) throws SQLException;
	
	// 将记录的属性值写入到数据库操作的PreparedStatement中，返回写入的属性个数
	int writePropertiesToPreparedStatement(PreparedStatement ps) throws SQLException;
}
