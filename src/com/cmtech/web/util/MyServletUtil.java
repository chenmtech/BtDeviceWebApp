package com.cmtech.web.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class MyServletUtil {
	public static void responseWithJson(HttpServletResponse resp, Map<String, String> data) {
		if(data == null || data.isEmpty()) return;
		
		JSONObject json = new JSONObject();
		for(Map.Entry<String, String> entry : data.entrySet()) {
			json.put(entry.getKey(), entry.getValue());
		}
		
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json; charset=utf-8");
		PrintWriter out = null;
		try {
			out = resp.getWriter();
			out.append(json.toString());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	

}
