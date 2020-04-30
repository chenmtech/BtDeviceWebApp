package com.cmtech.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.BleEcgReport10;
import com.cmtech.web.util.ServletUtil;

@WebServlet(name="ReportServlet", urlPatterns="/Report")
public class ReportServlet extends HttpServlet {

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
		JSONObject json = new JSONObject();
		json.put("id", id);
		ServletUtil.responseJson(resp, json);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
	}
	
	

}
