package com.cmtech.web.btdevice;

import org.json.JSONObject;

/**
 * 支持JSON操作的接口
 * @author gdmc
 *
 */
public interface IJsonable {
	// 从JSON Object初始化
    void fromJson(JSONObject json);
    
    // 将字段打包为JSON Object
    JSONObject toJson();
}
