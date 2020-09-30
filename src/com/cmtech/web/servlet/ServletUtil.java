package com.cmtech.web.servlet;

import static com.cmtech.web.btdevice.ReturnCode.SUCCESS;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.ReturnCode;

public class ServletUtil {
	
	public static void dataResponse(HttpServletResponse resp, JSONObject data) throws IOException {
		if(resp == null || data == null) {
			throw new NullPointerException();
		}
		data.put("code", SUCCESS.getCode());
		doResponse(resp, data);
	}
	
	public static void codeResponse(HttpServletResponse resp, ReturnCode code) throws IOException {
		if(resp == null || code == null) {
			throw new NullPointerException();
		}
		
		JSONObject json = new JSONObject();
		json.put("code", code.getCode());
		ServletUtil.doResponse(resp, json);
		//System.out.println(exception.getDescription());
	}
	
	public static void doResponse(HttpServletResponse resp, JSONObject json) throws IOException {
		if(resp == null || json == null) {
			throw new NullPointerException();
		}
		
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json; charset=utf-8");
		try(PrintWriter out = resp.getWriter()) {
			out.append(json.toString());
			out.flush();
		}
	}
}
