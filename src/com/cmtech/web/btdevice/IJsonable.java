package com.cmtech.web.btdevice;

import org.json.JSONObject;

public interface IJsonable {
    void fromJson(JSONObject json);
    JSONObject toJson();
}
