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
import javax.servlet.http.HttpSession;

import com.cmtech.web.dbop.Account;
import com.cmtech.web.dbop.MySQLUtil;

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
		// TODO Auto-generated method stub
		//super.doGet(req, resp);
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=utf-8");
		PrintWriter out = resp.getWriter();
		HttpSession session = req.getSession();
		out.println("<HTML>"); 
		out.println("<HEAD>");
		out.println("<TITLE> Hi, Im Chenming.</TITLE>");
		out.println("</HEAD>");
		out.println("<BODY>");
		if(session.isNew())
			out.println("你好，初始见面。");
		else {
			out.println("你好，欢迎回来。");
			session.invalidate();
		}
		out.println("</BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
		System.out.println("doGet");
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				MySQLUtil.connect();
				Account acnt = new Account("chenm", "ctl080512");
				if(acnt.insert()) {
					System.out.println("插入成功,id="+acnt.getId());
				} else {
					System.out.println("插入失败");
				}
				MySQLUtil.disconnect();
			}			
		}).start();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//super.doPost(req, resp);
		System.out.println("doPost");
	}

	
}
