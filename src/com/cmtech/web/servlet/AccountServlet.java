/**
 * Project Name:BtDeviceWebApp
 * File Name:RecvEcgRecord.java
 * Package Name:webproc
 * Date:2020��4��9������7:18:21
 * Copyright (c) 2020, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.web.servlet;

import static com.cmtech.web.btdevice.ReturnCode.DOWNLOAD_ERR;
import static com.cmtech.web.btdevice.ReturnCode.INVALID_PARA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.LOGIN_ERR;
import static com.cmtech.web.btdevice.ReturnCode.OTHER_ERR;
import static com.cmtech.web.btdevice.ReturnCode.SIGNUP_ERR;
import static com.cmtech.web.btdevice.ReturnCode.SUCCESS;
import static com.cmtech.web.btdevice.ReturnCode.UPLOAD_ERR;
import static com.cmtech.web.MyConstant.*;

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
		String platName = req.getParameter("platName");
		String platId = req.getParameter("platId");
		
		if(cmd == null || platName == null || platId == null) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
		} else {
			Account acount = new Account(platName, platId);
			//System.out.println(platName+platId);
			switch(cmd) {
			case "login":
				if(acount.login()) {
					ServletUtil.codeResponse(resp, SUCCESS);
				} else {
					ServletUtil.codeResponse(resp, LOGIN_ERR);
				}
				break;
				
			case "signUp":
				if(acount.signUp()) {
					ServletUtil.codeResponse(resp, SUCCESS);
				} else {
					ServletUtil.codeResponse(resp, SIGNUP_ERR);
				}
				break;
				
			case "signUporLogin":
				if(acount.login() || acount.signUp()) {
					ServletUtil.codeResponse(resp, SUCCESS);
				} else {
					ServletUtil.codeResponse(resp, OTHER_ERR);
				}
				break;
				
				default:
					ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
					break;
			}
		}
	}

	/**
	 *  Upload a account
	 *  Download a account
	 *  Return the result with json object
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader streamReader = null;
		try {
			String charEncoding = request.getCharacterEncoding();
	        if (charEncoding == null) {
	            charEncoding = "UTF-8";
	        }
			streamReader = new BufferedReader( new InputStreamReader(request.getInputStream(), charEncoding));
			StringBuilder strBuilder = new StringBuilder();
			String inputStr;
			while ((inputStr = streamReader.readLine()) != null)
				strBuilder.append(inputStr);
			JSONObject jsonObject = new JSONObject(strBuilder.toString());
			System.out.println(jsonObject.toString());
			
			String platName = jsonObject.getString("platName");
			String platId = jsonObject.getString("platId");
			if(platName == null || platId == null) {
				ServletUtil.codeResponse(response, INVALID_PARA_ERR);
				return;
			}
			Account account = new Account(platName, platId);
			String cmd = jsonObject.getString("cmd");
			boolean result;
			int accountId = INVALID_ID;
			switch(cmd) {
			case "upload":
				account.fromJson(jsonObject);
				accountId = account.getId();
				if(accountId == INVALID_ID) {
					result = account.insert();
				} else {
					result = account.update();
				}
				
				if(result) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, UPLOAD_ERR);
				}
				break;
				
			case "download":
				if(account.retrieve()) {
					JSONObject json = new JSONObject();
					json.put("account", account.toJson());
					ServletUtil.dataResponse(response, json);
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
			ServletUtil.codeResponse(response, OTHER_ERR);
		} finally {
			streamReader.close();
		}
	}
	
}
