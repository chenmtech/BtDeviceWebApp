package com.cmtech.web.servlet;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;
import static com.cmtech.web.exception.MyExceptionCode.ACCOUNT_ERR;
import static com.cmtech.web.exception.MyExceptionCode.DOWNLOAD_ERR;
import static com.cmtech.web.exception.MyExceptionCode.INVALID_PARA_ERR;
import static com.cmtech.web.exception.MyExceptionCode.OTHER_ERR;
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

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.BleEcgReport10;
import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.dbUtil.EcgReportDbUtil;
import com.cmtech.web.dbUtil.RecordDbUtil;
import com.cmtech.web.exception.MyException;
import com.cmtech.web.util.Base64;

@WebServlet(name="ReportServlet", urlPatterns="/Report")
public class ReportServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletUtil.responseException(resp, new MyException(OTHER_ERR, "网络错误"));
	}

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
			if(Account.getId(platName, platId) == INVALID_ID) {
				ServletUtil.responseException(response, new MyException(ACCOUNT_ERR, "无效用户"));
				return;
			}			
					
			// 执行命令
			String cmd = jsonObject.getString("cmd");
			boolean cmdResult = false;
			JSONObject jsonResult = null;
			switch(cmd) {
			
			case "request":
		        long recordCreateTime = jsonObject.getLong("recordCreateTime");
		        String recordDevAddress = jsonObject.getString("recordDevAddress");
		        String ver = jsonObject.getString("ver");
		        long createTime = jsonObject.getLong("createTime");
		        String content = jsonObject.getString("content");
		        
		        JSONObject report = EcgReportDbUtil.requestReport(recordCreateTime, recordDevAddress, createTime, content);
				
		        System.out.println(report.toString());
		        
				jsonResult = new JSONObject();
				jsonResult.put("code", SUCCESS.ordinal());
				jsonResult.put("report", report);
				ServletUtil.responseJson(response, jsonResult);
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
