package com.cmtech.web.servlet;

import static com.cmtech.web.btdevice.ReturnCode.SUCCESS;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.ReturnCode;

public class ServletUtil {	
	
	public static void dataResponse(HttpServletResponse resp, Object data) throws IOException {
		dataResponse(resp, "操作成功", data);
	}	 
	
	public static void dataResponse(HttpServletResponse resp, String msg, Object data) throws IOException {
		if(resp == null || msg == null) {
			throw new NullPointerException();
		}
		JSONObject json = new JSONObject();
		json.put("code", SUCCESS.getCode());
		json.put("message", msg);
		json.put("data", data);
		doResponse(resp, json);
	}	
	
	public static void codeResponse(HttpServletResponse resp, ReturnCode code) throws IOException {
		codeResponse(resp, code, "");
	}
	
	public static void codeResponse(HttpServletResponse resp, ReturnCode code, String msg) throws IOException {
		if(resp == null || code == null || msg == null) {
			throw new NullPointerException();
		}
		
		JSONObject json = new JSONObject();
		json.put("code", code.getCode());
		json.put("message", msg);
		doResponse(resp, json);
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
