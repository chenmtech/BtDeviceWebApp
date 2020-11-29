/**
 * Project Name:BtDeviceWebApp
 * File Name:RecvEcgRecord.java
 * Package Name:webproc
 * Date:2020��4��9������7:18:21
 * Copyright (c) 2020, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.web.servlet;

import static com.cmtech.web.MyConstant.INVALID_ID;
import static com.cmtech.web.btdevice.ReturnCode.ACCOUNT_ERR;
import static com.cmtech.web.btdevice.ReturnCode.CHANGE_PASSWORD_ERR;
import static com.cmtech.web.btdevice.ReturnCode.DATA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.DOWNLOAD_ERR;
import static com.cmtech.web.btdevice.ReturnCode.INVALID_PARA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.LOGIN_ERR;
import static com.cmtech.web.btdevice.ReturnCode.SIGNUP_ERR;
import static com.cmtech.web.btdevice.ReturnCode.SUCCESS;
import static com.cmtech.web.btdevice.ReturnCode.UPLOAD_ERR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;

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

	/**
	 * 	SignUp or Login using platName and platId
	 *  Return isSuccess and errStr with json
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String cmd = req.getParameter("cmd");
		
		if(cmd == null) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
			return;
		}
		String userName = req.getParameter("userName");
		String password = req.getParameter("password");
		if(userName == null || password == null) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
			return;
		}
		
		if(cmd.equals("signUp")) {
			if(Account.signUp(userName, password)) {
				ServletUtil.codeResponse(resp, SUCCESS);
			} else {
				ServletUtil.codeResponse(resp, SIGNUP_ERR);
			}
			return;
		}
		
		if(cmd.equals("login")) {
			int id = Account.login(userName, password);
			
			if(id == INVALID_ID) {
				ServletUtil.codeResponse(resp, LOGIN_ERR);
			} else {
				JSONObject json = new JSONObject();
				json.put("id", id);
				ServletUtil.contentResponse(resp, json);
			}
			return;
		}
		
		if(cmd.equals("changePassword")) {
			if(Account.changePassword(userName, password)) {
				ServletUtil.codeResponse(resp, SUCCESS);
			} else {
				ServletUtil.codeResponse(resp, CHANGE_PASSWORD_ERR);
			}
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
		return;
	}

	/**
	 *  Upload a account
	 *  Download a account
	 *  Return the result with json object
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
		
		try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), charEncoding))) {
			StringBuilder strBuilder = new StringBuilder();
			String s;
			while ((s = streamReader.readLine()) != null)
				strBuilder.append(s);
			JSONObject inputJson = new JSONObject(strBuilder.toString());
			// System.out.println(inputJson.toString());
			
			// 验证账户是否有效
			int id = inputJson.getInt("id");
			if(!Account.isAccountValid(id)) {
				ServletUtil.codeResponse(response, ACCOUNT_ERR);
				return;
			}
			
			Account account = new Account(id);
			
			String cmd = inputJson.getString("cmd");
			switch(cmd) {
			case "upload":
				account.fromJson(inputJson);
				if(account.update()) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, UPLOAD_ERR);
				}
				break;
				
			case "download":
				if(account.retrieve()) {
					ServletUtil.contentResponse(response, account.toJson());
				} else {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				}
				break;
				
				default:
					ServletUtil.codeResponse(response, INVALID_PARA_ERR);
					break;				
			}
		} catch (Exception e) {
			e.printStackTrace();
			ServletUtil.codeResponse(response, DATA_ERR);
		}
	}
	
}
