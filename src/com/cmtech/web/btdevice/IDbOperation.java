package com.cmtech.web.btdevice;

/**
 * 数据库操作接口
 * @author gdmc
 *
 */
public interface IDbOperation {
	int getId(); // 得到数据库中本条目的ID
	boolean retrieve(); // 从数据库获取本条目的信息
	boolean insert(); // 将本条目插入到数据库
	boolean delete(); // 从数据库删除本条目
	boolean update(); // 更新数据库中的本条目信息
}
