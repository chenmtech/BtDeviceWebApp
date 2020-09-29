package com.cmtech.web.btdevice;

import org.json.JSONObject;

public interface IJsonable {
    JSONObject toJson();
    void fromJson(JSONObject json);
}
