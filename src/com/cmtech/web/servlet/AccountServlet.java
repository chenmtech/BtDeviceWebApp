/**
 * Project Name:BtDeviceWebApp
 * File Name:RecvEcgRecord.java
 * Package Name:webproc
 * Date:2020��4��9������7:18:21
 * Copyright (c) 2020, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.web.servlet;

import static com.cmtech.web.exception.MyExceptionCode.INVALID_PARA_ERR;
import static com.cmtech.web.exception.MyExceptionCode.LOGIN_ERR;
import static com.cmtech.web.exception.MyExceptionCode.OTHER_ERR;
import static com.cmtech.web.exception.MyExceptionCode.SIGNUP_ERR;
import static com.cmtech.web.exception.MyExceptionCode.NO_ERR;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.exception.MyException;
import com.cmtech.web.util.AccountUtil;
import com.cmtech.web.util.MyServletUtil;

/**
 * ClassName: AccountServlet
 * Function: TODO ADD FUNCTION. 
 * Reason: TODO ADD REASON(��ѡ). 
 * date: 2020��4��9�� ����7:18:21 
 *
 * @author bme
 * @version 
 * @since JDK 1.6
 */
@WebServlet(name="AccountServlet", urlPatterns="/Account")
public class AccountServlet extends HttpServlet {

	/**
	 * serialVersionUID:TODO(��һ�仰�������������ʾʲô).
	 * @since JDK 1.6
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// SignUp or Login using platName and platId
		// return isSuccess and errStr with json
		String cmd = req.getParameter("cmd");
		String platName = req.getParameter("platName");
		String platId = req.getParameter("platId");
		if(cmd == null || platName == null || platId == null) {
			response(resp, new MyException(INVALID_PARA_ERR, "无效请求"));
		} else {
			Account acnt = new Account(platName, platId);
			
			if(cmd.equals("login")) {
				if(AccountUtil.login(acnt)) {
					response(resp, new MyException(NO_ERR, "登录成功"));
				} else {
					response(resp, new MyException(LOGIN_ERR, "账户不存在，登录错误"));
				}
			} 
			
			else if(cmd.equals("signUp")) {
				if(AccountUtil.signUp(acnt)) {
					response(resp, new MyException(NO_ERR, "注册成功"));
				} else {
					response(resp, new MyException(SIGNUP_ERR, "注册失败"));
				}
			}
			
			else if(cmd.equals("signUporLogin")) {
				if(AccountUtil.login(acnt) || AccountUtil.signUp(acnt)) {
					response(resp, new MyException(NO_ERR, "注册/登录成功"));
				} else {
					response(resp, new MyException(OTHER_ERR, "注册/登录失败"));
				}
			}
			
			else {
				response(resp, new MyException(INVALID_PARA_ERR, "无效请求"));
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}
	
	private void response(HttpServletResponse resp, MyException exception) {
		JSONObject json = new JSONObject();
		json.put("code", exception.getCode().ordinal());
		json.put("errStr", exception.getDescription());
		
		MyServletUtil.responseWithJson(resp, json);
		
		System.out.println(exception.getDescription());
	}

	
}
