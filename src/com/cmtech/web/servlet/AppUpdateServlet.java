package com.cmtech.web.servlet;

import static com.cmtech.web.btdevice.ReturnCode.DATA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.DOWNLOAD_ERR;
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
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String sver = req.getParameter("sver");
		
		if(sver == null || sver.equals("1.0")) {
			WebCommandService10.doAppUpdateGet(req, resp);
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
	}

	
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
			ServletUtil.codeResponse(resp, DATA_ERR);
			return;
		}
		
		if(!reqJson.has("sver") || reqJson.getString("sver").equals("1.0")) {
			WebCommandService10.doAppUpdatePost(reqJson, resp);
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
	}
}
