/**
 * Project Name:BtDeviceWebApp
 * File Name:RecvEcgRecord.java
 * Package Name:webproc
 * Date:2020��4��9������7:18:21
 * Copyright (c) 2020, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.dbop.Account;
import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

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
		// Login or SignUp using platName and platId
		// return the id of the account with json
		// if failure, id = INVALID_ID
		String platName = req.getParameter("platName");
		String platId = req.getParameter("platId");
		if(platName == null || platId == null) {
			System.out.println("插入失败");
			response(resp, INVALID_ID);
			return;
		}
		
		int id = Account.getId(platName, platId);
		if(id == INVALID_ID) {
			Account acnt = new Account(platName, platId);
			if(acnt.insert()) {
				id = acnt.getId();
				System.out.println("插入成功,id="+id);
			} else {
				System.out.println("插入失败");
			}
		} else {
			System.out.println("账户已存在");
		}
		
		response(resp, id);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}
	
	private void response(HttpServletResponse resp, int id) {
		JSONObject json = new JSONObject();
		json.put("id", id);
		
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
