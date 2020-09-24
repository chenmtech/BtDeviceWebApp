package com.cmtech.web.dbUtil;

import org.json.JSONArray;

public interface IRecordDbOperatable {
	int retrieveId();
	boolean retrieve();
	boolean insert();
	boolean delete();
	boolean updateNote();
	JSONArray downloadBasicInfo(String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num);
}