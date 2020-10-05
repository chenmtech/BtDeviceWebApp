package com.cmtech.web.servlet;

import static com.cmtech.web.btdevice.ReturnCode.*;
import static com.cmtech.web.MyConstant.*;

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
import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.dbUtil.RecordWebUtil;


/**
 * Servlet implementation class RecordServlet
 */
@WebServlet(name="RecordServlet", urlPatterns="/Record")
public class RecordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecordServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 	Query the record using recordTypeCode, createTime and devAddress
	 *  Return the id of the record with json
	 *  If not exist, return INVALID_ID
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String strRecordTypeCode = request.getParameter("recordTypeCode");
		String strCreateTime = request.getParameter("createTime");
		String devAddress = request.getParameter("devAddress");
		if(strRecordTypeCode == null || strCreateTime == null || devAddress == null) {
			ServletUtil.codeResponse(response, INVALID_PARA_ERR);
			return;
		}
		RecordType type = RecordType.fromCode( Integer.parseInt(strRecordTypeCode) );
		long createTime = Long.parseLong(strCreateTime);
		
		int id = RecordWebUtil.getId(type, createTime, devAddress);
		JSONObject json = new JSONObject();
		json.put("id", id);
		ServletUtil.dataResponse(response, json);
		
		System.out.println("The found record id = "+id);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 *  Upload a record
	 *  Update the note of a record
	 *  Download some record basic information
	 *  Download a record
	 *  Delete a record
	 *  Return the result with json object
	 */
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
			
			// 验证用户是否有效
			String platName = jsonObject.getString("platName");
			String platId = jsonObject.getString("platId");
			if(platName == null || platId == null) {
				ServletUtil.codeResponse(response, INVALID_PARA_ERR);
				return;
			}
			if(new Account(platName, platId).getId() == INVALID_ID) {
				ServletUtil.codeResponse(response, ACCOUNT_ERR);
				return;
			}			

			// 执行命令
			String cmd = jsonObject.getString("cmd");
			RecordType type = null;
			if(!jsonObject.isNull("recordTypeCode"))
				type = RecordType.fromCode(jsonObject.getInt("recordTypeCode"));
			
			boolean cmdResult = false;
			JSONObject jsonResult = null;
			long createTime;
			String devAddress;
			switch(cmd) {			
			case "upload":
				cmdResult = RecordWebUtil.upload(type, jsonObject);
				if(cmdResult) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, UPLOAD_ERR);
				}
				break;
				
			case "updateNote":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				String note = jsonObject.getString("note");
				cmdResult = RecordWebUtil.updateNote(type, createTime, devAddress, note);
				if(cmdResult) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, UPDATE_ERR);
				}
				break;
				
			case "downloadBasicInfo":
				long fromTime = jsonObject.getLong("fromTime");
				String creatorPlat = jsonObject.getString("creatorPlat");
				String creatorId = jsonObject.getString("creatorId");
				int num = jsonObject.getInt("num");
				String noteSearchStr = jsonObject.getString("noteSearchStr");
				JSONArray jsonArr = RecordWebUtil.downloadBasicInfo(type, creatorPlat, creatorId, fromTime, noteSearchStr, num);
				
				if(jsonArr == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					System.out.println(jsonArr.toString());
					jsonResult = new JSONObject();
					jsonResult.put("records", jsonArr);
					ServletUtil.dataResponse(response, jsonResult);
				}
				break;
				
			case "download":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				JSONObject json = RecordWebUtil.download(type, createTime, devAddress);
				
				if(json == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					System.out.println(json.toString());
					jsonResult = new JSONObject();
					jsonResult.put("record", json);
					ServletUtil.dataResponse(response, jsonResult);
				}
				break;
				
			case "delete":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				cmdResult = RecordWebUtil.delete(type, createTime, devAddress);
				if(cmdResult) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, DELETE_ERR);
				}
				break;
				
			case "downloadReport":
				createTime = jsonObject.getLong("createTime");
		        devAddress = jsonObject.getString("devAddress");
		        JSONObject json1 = RecordWebUtil.downloadDiagnoseResult(createTime, devAddress);		        
				
				if(json1 == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					System.out.println(json1.toString());
					jsonResult = new JSONObject();
					jsonResult.put("reportResult", json1);
					ServletUtil.dataResponse(response, jsonResult);
				}
				break;
				
			case "requestReport":
				createTime = jsonObject.getLong("createTime");
		        devAddress = jsonObject.getString("devAddress");
		        JSONObject report = RecordWebUtil.requestDiagnose(createTime, devAddress);		        
				
				if(report == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					System.out.println(report.toString());
					jsonResult = new JSONObject();
					jsonResult.put("reportResult", report);
					ServletUtil.dataResponse(response, jsonResult);
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
