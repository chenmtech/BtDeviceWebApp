package com.cmtech.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.exception.MyException;

public class ServletUtil {
	
	public static void responseJson(HttpServletResponse resp, JSONObject json) throws IOException {
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
	
	public static void responseException(HttpServletResponse resp, MyException exception) throws IOException {
		JSONObject json = new JSONObject();
		json.put("code", exception.getCode().ordinal());
		json.put("errStr", exception.getDescription());
		ServletUtil.responseJson(resp, json);
		System.out.println(exception.getDescription());
	}
}
