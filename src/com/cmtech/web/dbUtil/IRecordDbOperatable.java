package com.cmtech.web.dbUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IRecordDbOperatable {
	int retrieveId(long createTime, String devAddress);
	boolean upload(JSONObject json);
	JSONObject download(long createTime, String devAddress);
	boolean delete(long createTime, String devAddress);
	boolean updateNote(long createTime, String devAddress, String note);
	JSONArray downloadBasicInfo(String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num);
}
