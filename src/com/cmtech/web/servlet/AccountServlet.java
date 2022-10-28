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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.BasicRecord;
import com.cmtech.web.btdevice.ShareInfo;
import com.cmtech.web.dbUtil.RecordWebUtil;

/**
 * ClassName: AccountServlet
 * Function: 账户Servlet，响应手机端对账户的有关操作
 * date: 2020年4月
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
	 * 	实现手机端登录、注册和修改密码操作
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
		
		// 注册新用户
		if(cmd.equals("signUp")) {
			if(Account.signUp(userName, password)) {
				ServletUtil.codeResponse(resp, SUCCESS);
			} else {
				ServletUtil.codeResponse(resp, SIGNUP_ERR);
			}
			return;
		}
		
		// 登录，如果成功，返回账户ID号
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
		
		// 修改密码
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
	 *  实现手机端用户信息的上传（更新）和下载操作
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
			
			// 上传
			case "upload":
				account.fromJson(inputJson);
				if(account.update()) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, UPLOAD_ERR);
				}
				break;
				
			// 下载	
			case "download":
				if(account.retrieve()) {
					ServletUtil.contentResponse(response, account.toJson());
				} else {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				}
				break;
				
			case "downloadShareInfo":
				List<ShareInfo> found = ShareInfo.retrieveShareInfo(id);
				if(found == null || found.isEmpty()) 
					ServletUtil.codeResponse(response, SUCCESS);
				else {				
					JSONArray jsonArray = new JSONArray();
					for(ShareInfo shareInfo : found) {
						jsonArray.put(shareInfo.toJson());
					}
					ServletUtil.contentResponse(response, jsonArray);
				}				
				break;
				
			case "changeShareInfo":
				int fromId = inputJson.getInt("fromId");
				int status = inputJson.getInt("status");
				if(ShareInfo.changeStatus(fromId, id, status))
					ServletUtil.codeResponse(response, SUCCESS);
				else
					ServletUtil.codeResponse(response, DATA_ERR);
				break;
				
			case "addShare":
				int toId = inputJson.getInt("toId");
				if(Account.exist(toId) && ShareInfo.getId(id, toId) == INVALID_ID && ShareInfo.insert(id, toId))
					ServletUtil.codeResponse(response, SUCCESS);
				else
					ServletUtil.codeResponse(response, DATA_ERR);
				break;	
				
			case "downloadContactPerson":
				int contactId = inputJson.getInt("contactId");
				Account contact = new Account(contactId);
				if(contact.retrieve()) {
					ServletUtil.contentResponse(response, contact.contactInfoToJson());
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
