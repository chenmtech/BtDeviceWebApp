package com.cmtech.web.servlet;

import static com.cmtech.web.btdevice.ReturnCode.DOWNLOAD_ERR;
import static com.cmtech.web.btdevice.ReturnCode.INVALID_PARA_ERR;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.AppUpdateInfo;

@WebServlet(name="AppUpdateServlet", urlPatterns="/AppUpdateInfo")
public class AppUpdateServlet extends HttpServlet {

	/**
	 * serialVersionUID:TODO(��һ�仰�������������ʾʲô).
	 * @since JDK 1.6
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *  Get App Update Info
	 * 	Return App Update Info with json
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cmd = request.getParameter("cmd");
		if(!"download".equals(cmd)) {
			ServletUtil.codeResponse(response, INVALID_PARA_ERR);
		} else {
			AppUpdateInfo updateInfo = new AppUpdateInfo();
			if(updateInfo.retrieve()) {
				JSONObject json = new JSONObject();
				json.put("appUpdateInfo", updateInfo.toJson());
				ServletUtil.jsonResponse(response, json);
			} else {
				ServletUtil.codeResponse(response, DOWNLOAD_ERR);
			}
		}
	}

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
}
