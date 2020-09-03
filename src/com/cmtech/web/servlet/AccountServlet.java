/**
 * Project Name:BtDeviceWebApp
 * File Name:RecvEcgRecord.java
 * Package Name:webproc
 * Date:2020��4��9������7:18:21
 * Copyright (c) 2020, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.web.servlet;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;
import static com.cmtech.web.exception.MyExceptionCode.ACCOUNT_ERR;
import static com.cmtech.web.exception.MyExceptionCode.DOWNLOAD_ERR;
import static com.cmtech.web.exception.MyExceptionCode.INVALID_PARA_ERR;
import static com.cmtech.web.exception.MyExceptionCode.LOGIN_ERR;
import static com.cmtech.web.exception.MyExceptionCode.OTHER_ERR;
import static com.cmtech.web.exception.MyExceptionCode.SIGNUP_ERR;
import static com.cmtech.web.exception.MyExceptionCode.SUCCESS;
import static com.cmtech.web.exception.MyExceptionCode.UPLOAD_ERR;

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
import com.cmtech.web.exception.MyException;
import com.cmtech.web.util.Base64;

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
			ServletUtil.responseException(resp, new MyException(INVALID_PARA_ERR, "无效请求"));
		} else {
			Account acount = new Account(platName, platId);
			System.out.println(platName+platId);
			switch(cmd) {
			case "login":
				if(acount.login()) {
					ServletUtil.responseException(resp, new MyException(SUCCESS, "登录成功"));
				} else {
					ServletUtil.responseException(resp, new MyException(LOGIN_ERR, "登录错误"));
				}
				break;
				
			case "signUp":
				if(acount.signUp()) {
					ServletUtil.responseException(resp, new MyException(SUCCESS, "注册成功"));
				} else {
					ServletUtil.responseException(resp, new MyException(SIGNUP_ERR, "注册失败"));
				}
				break;
				
			case "signUporLogin":
				if(acount.login() || acount.signUp()) {
					ServletUtil.responseException(resp, new MyException(SUCCESS, "注册/登录成功"));
				} else {
					ServletUtil.responseException(resp, new MyException(OTHER_ERR, "注册/登录失败"));
				}
				break;
				
				default:
					ServletUtil.responseException(resp, new MyException(INVALID_PARA_ERR, "无效请求"));
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
				ServletUtil.responseException(response, new MyException(INVALID_PARA_ERR, "无效参数"));
				return;
			}
					
			String cmd = jsonObject.getString("cmd");
			boolean result;
			int accountId = INVALID_ID;
			switch(cmd) {
			case "upload":
				String name = jsonObject.getString("name");
				String note = jsonObject.getString("note");
				String iconStr = jsonObject.getString("iconStr");
				byte[] iconData = Base64.decode(iconStr, Base64.DEFAULT);
				Account account = new Account(platName, platId, name, note, iconData);
				
				accountId = Account.getId(platName, platId);
				if(accountId == INVALID_ID) {
					result = account.insert();
				} else {
					result = account.updateDb();
				}
				
				if(result) {
					ServletUtil.responseException(response, new MyException(SUCCESS, "上传成功"));
				} else {
					ServletUtil.responseException(response, new MyException(UPLOAD_ERR, "上传失败"));
				}
				break;
				
			case "download":
				accountId = Account.getId(platName, platId);
				if(accountId == INVALID_ID) {
					ServletUtil.responseException(response, new MyException(ACCOUNT_ERR, "无效账户"));
					return;
				}
				
				Account acount = Account.create(accountId);
				if(acount != null) {
					JSONObject json = new JSONObject();
					json.put("code", SUCCESS.ordinal());
					json.put("account", acount.toJson());
					ServletUtil.responseJson(response, json);
				} else {
					ServletUtil.responseException(response, new MyException(DOWNLOAD_ERR, "下载错误"));
				}
				break;
				
				default:
					ServletUtil.responseException(response, new MyException(INVALID_PARA_ERR, "无效命令"));
					break;				
			}
		} catch (Exception e) {
			e.printStackTrace();
			ServletUtil.responseException(response, new MyException(OTHER_ERR, "执行命令错误"));
		} finally {
			streamReader.close();
		}
	}
	
}
