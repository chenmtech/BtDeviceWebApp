package com.cmtech.web.dbUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IRecordDbOperatable {
	int retrieveId();
	boolean insert();
	JSONObject download(long createTime, String devAddress);
	boolean delete();
	boolean updateNote();
	JSONArray downloadBasicInfo(String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num);
}
