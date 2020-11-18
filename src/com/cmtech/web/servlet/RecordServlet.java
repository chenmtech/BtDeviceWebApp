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
import org.json.JSONException;
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
		ServletUtil.jsonResponse(response, json);
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
			//System.out.println(inputJson.toString());
			
			// 验证账户是否有效
			int accountId = inputJson.getInt("accountId");
			if(!Account.isAccountValid(accountId)) {
				ServletUtil.codeResponse(response, ACCOUNT_ERR);
				return;
			}

			// 执行命令
			String cmd = inputJson.getString("cmd");
			RecordType type = RecordType.fromCode(inputJson.getInt("recordTypeCode"));
			
			boolean cmdResult = false;
			JSONObject returnJson = null;
			long createTime = INVALID_TIME;
			String devAddress;
			switch(cmd) {
			case "upload":
				cmdResult = RecordWebUtil.upload(type, inputJson);
				if(cmdResult) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, UPLOAD_ERR);
				}
				break;
				
			case "download":
				createTime = inputJson.getLong("createTime");
				devAddress = inputJson.getString("devAddress");
				JSONObject json = RecordWebUtil.download(type, createTime, devAddress);
				
				if(json == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					System.out.println(json.toString());
					returnJson = new JSONObject();
					returnJson.put("record", json);
					ServletUtil.jsonResponse(response, returnJson);
				}
				break;
				
			case "delete":
				createTime = inputJson.getLong("createTime");
				devAddress = inputJson.getString("devAddress");
				cmdResult = RecordWebUtil.delete(type, createTime, devAddress);
				if(cmdResult) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, DELETE_ERR);
				}
				break;
				
			case "downloadList":
				long fromTime = inputJson.getLong("fromTime");
				int creatorId = inputJson.getInt("creatorId");
				int num = inputJson.getInt("num");
				String noteSearchStr = inputJson.getString("noteSearchStr");
				JSONArray jsonArr = RecordWebUtil.downloadList(type, creatorId, fromTime, noteSearchStr, num);
				
				//System.out.println(jsonArr.toString());
				returnJson = new JSONObject();
				returnJson.put("records", jsonArr);
				ServletUtil.jsonResponse(response, returnJson);
				break;
				
			case "downloadReport":
				createTime = inputJson.getLong("createTime");
		        devAddress = inputJson.getString("devAddress");
		        JSONObject json1 = RecordWebUtil.downloadDiagnoseReport(createTime, devAddress);		        
				
				if(json1 == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					System.out.println(json1.toString());
					returnJson = new JSONObject();
					returnJson.put("reportResult", json1);
					ServletUtil.jsonResponse(response, returnJson);
				}
				break;
				
			case "requestReport":
				createTime = inputJson.getLong("createTime");
		        devAddress = inputJson.getString("devAddress");
		        JSONObject report = RecordWebUtil.requestDiagnose(createTime, devAddress);		        
				
				if(report == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					System.out.println(report.toString());
					returnJson = new JSONObject();
					returnJson.put("reportResult", report);
					ServletUtil.jsonResponse(response, returnJson);
				}
				break;	
				
				default:
					ServletUtil.codeResponse(response, INVALID_PARA_ERR);
					break;
			}
        } catch (JSONException e) {
			e.printStackTrace();
			ServletUtil.codeResponse(response, DATA_ERR);
		} 
	}
}
