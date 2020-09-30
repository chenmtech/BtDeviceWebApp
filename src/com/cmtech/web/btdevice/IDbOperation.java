package com.cmtech.web.btdevice;

public interface IDbOperation {
	int getId(); // 得到数据库中记录的ID
	boolean retrieve(); // 从数据库获取记录的信息
	boolean insert(); // 插入一条记录到数据库
	boolean delete(); // 删除一条记录
	boolean update(); // 更新数据库中的记录信息
}
