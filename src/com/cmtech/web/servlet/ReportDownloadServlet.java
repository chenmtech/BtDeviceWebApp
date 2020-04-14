package com.cmtech.web.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cmtech.web.btdevice.BleEcgReport10;
import com.cmtech.web.util.MyServletUtil;

@WebServlet(name="ReportDownloadServlet", urlPatterns="/ReportDownload")
public class ReportDownloadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String recordIdStr = req.getParameter("recordId");
		int recordId = Integer.parseInt(recordIdStr);
		
		int id = BleEcgReport10.getId(recordId);
		
		Map<String, String> data = new HashMap<>();
		data.put("id", String.valueOf(id));
		MyServletUtil.responseWithJson(resp, data);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
	}
	
	

}
