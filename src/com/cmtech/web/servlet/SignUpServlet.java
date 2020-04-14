/**
 * Project Name:BtDeviceWebApp
 * File Name:RecvEcgRecord.java
 * Package Name:webproc
 * Date:2020��4��9������7:18:21
 * Copyright (c) 2020, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.web.servlet;

import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cmtech.web.dbop.Account;
import com.cmtech.web.util.MyServletUtil;

/**
 * ClassName: RecvEcgRecord
 * Function: TODO ADD FUNCTION. 
 * Reason: TODO ADD REASON(��ѡ). 
 * date: 2020��4��9�� ����7:18:21 
 *
 * @author bme
 * @version 
 * @since JDK 1.6
 */
@WebServlet(name="SignUpServlet", urlPatterns="/SignUp")
public class SignUpServlet extends HttpServlet {

	/**
	 * serialVersionUID:TODO(��һ�仰�������������ʾʲô).
	 * @since JDK 1.6
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// SignUp using platName and platId
		// return isSuccess and errStr with json
		String platName = req.getParameter("platName");
		String platId = req.getParameter("platId");
		if(platName == null || platId == null) {
			System.out.println("无效注册参数");
			response(resp, false, "无效注册参数");
		} else {
			Account acnt = new Account(platName, platId);
			if(acnt.getId() == INVALID_ID && !acnt.insert()) {
				System.out.println("注册失败");
				response(resp, false, "注册失败");
			} else {
				System.out.println("注册成功,id="+acnt.getId());
				response(resp, true, "");
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}
	
	private void response(HttpServletResponse resp, boolean isSuccess, String errStr) {
		Map<String, String> data = new HashMap<>();
		data.put("isSuccess", String.valueOf(isSuccess));
		data.put("errStr", errStr);
		
		MyServletUtil.responseWithJson(resp, data);
	}

	
}
