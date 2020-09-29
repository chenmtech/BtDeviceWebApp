package com.cmtech.web.btdevice;

public interface IDbOperation {
	int getId();
	boolean retrieve();
	boolean insert();
	boolean delete();
	boolean update();
}
