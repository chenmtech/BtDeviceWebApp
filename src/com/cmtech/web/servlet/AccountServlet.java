/**
 * Project Name:BtDeviceWebApp
 * File Name:RecvEcgRecord.java
 * Package Name:webproc
 * Date:2020��4��9������7:18:21
 * Copyright (c) 2020, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.web.servlet;

import static com.cmtech.web.btdevice.ReturnCode.DATA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.INVALID_PARA_ERR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

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
		String sver = req.getParameter("sver");
		
		if(sver == null || sver.equals("1.0")) {
			WebCommandService10.doAccountGet(req, resp);
			return;
		}
		
		if(sver.equals("1.1")) {
			WebCommandService11.doAccountGet(req, resp);
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
	}

	/**
	 *  实现手机端用户信息的上传（更新）和下载操作
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String charEncoding = req.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
		
        JSONObject reqJson = null;
		try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(req.getInputStream(), charEncoding))) {
			StringBuilder strBuilder = new StringBuilder();
			String s;
			while ((s = streamReader.readLine()) != null)
				strBuilder.append(s);
			reqJson = new JSONObject(strBuilder.toString());
		} catch (Exception e) {
			e.printStackTrace();
			ServletUtil.codeResponse(resp, DATA_ERR, "数据错误");
			return;
		}
		
		if(!reqJson.has("sver") || reqJson.getString("sver").equals("1.0")) {
			WebCommandService10.doAccountPost(reqJson, resp);
			return;
		}
		
		if(reqJson.getString("sver").equals("1.1")) {
			WebCommandService11.doAccountPost(reqJson, resp);
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
	}	
}
