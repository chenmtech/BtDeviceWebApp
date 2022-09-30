package com.cmtech.web.servlet;

import static com.cmtech.web.MyConstant.INVALID_TIME;
import static com.cmtech.web.btdevice.ReturnCode.ACCOUNT_ERR;
import static com.cmtech.web.btdevice.ReturnCode.DATA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.DELETE_ERR;
import static com.cmtech.web.btdevice.ReturnCode.DOWNLOAD_ERR;
import static com.cmtech.web.btdevice.ReturnCode.INVALID_PARA_ERR;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.dbUtil.RecordWebUtil;


/**
 * Servlet implementation class RecordServlet
 * 为手机端App提供记录操作的Servlet类
 * 注意：PC端App不需要这个类，它是通过直接连接数据库来操作记录的
 */
@WebServlet(name="RecordServlet", urlPatterns="/Record")
public class RecordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//---------------------------------------------------------- 记录操作命令
	// 上传记录
	private static final String CMD_UPLOAD = "upload";
	
	// 下载记录
	private static final String CMD_DOWNLOAD = "download";
	
	// 删除记录
	private static final String CMD_DELETE = "delete";
	
	// 下载记录列表
	private static final String CMD_DOWNLOAD_RECORD_LIST = "downloadRecordList";
	
	// 获取记录诊断报告
	private static final String CMD_RETRIEVE_DIAGNOSE_REPORT = "retrieveDiagnoseReport";
	
	
       
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
		ServletUtil.contentResponse(response, json);
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
			RecordType type;			
			boolean cmdResult = false;
			long createTime = INVALID_TIME;
			String devAddress;
			
			switch(cmd) {
			case CMD_UPLOAD:
				type = RecordType.fromCode(inputJson.getInt("recordTypeCode"));
				cmdResult = RecordWebUtil.upload(type, inputJson);
				if(cmdResult) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, UPLOAD_ERR);
				}
				break;
				
			case CMD_DOWNLOAD:
				type = RecordType.fromCode(inputJson.getInt("recordTypeCode"));
				createTime = inputJson.getLong("createTime");
				devAddress = inputJson.getString("devAddress");
				JSONObject json = RecordWebUtil.download(type, createTime, devAddress);
				
				if(json == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					//System.out.println(json.toString());
					ServletUtil.contentResponse(response, json);
				}
				break;
				
			case CMD_DELETE:
				type = RecordType.fromCode(inputJson.getInt("recordTypeCode"));
				createTime = inputJson.getLong("createTime");
				devAddress = inputJson.getString("devAddress");
				cmdResult = RecordWebUtil.delete(type, createTime, devAddress);
				if(cmdResult) {
					ServletUtil.codeResponse(response, SUCCESS);
				} else {
					ServletUtil.codeResponse(response, DELETE_ERR);
				}
				break;
				
			case CMD_DOWNLOAD_RECORD_LIST:
				String typeStr = inputJson.getString("recordTypeCode");
				String[] typeStrArr = typeStr.split(",");
				RecordType[] types = new RecordType[typeStrArr.length];
				for(int i = 0; i < typeStrArr.length; i++) {
					types[i] = RecordType.fromCode(Integer.parseInt(typeStrArr[i]));
				}
				long fromTime = inputJson.getLong("fromTime");
				int creatorId = inputJson.getInt("creatorId");
				int num = inputJson.getInt("num");
				String filterStr = inputJson.getString("filterStr");
				JSONArray jsonArr = RecordWebUtil.downloadRecordList(types, creatorId, fromTime, filterStr, num);
				if(jsonArr == null)
					ServletUtil.codeResponse(response, SUCCESS);
				else
					ServletUtil.contentResponse(response, jsonArr);
				break;
				
			case CMD_RETRIEVE_DIAGNOSE_REPORT:
				createTime = inputJson.getLong("createTime");
		        devAddress = inputJson.getString("devAddress");
		        JSONObject reportJson = RecordWebUtil.retrieveDiagnoseReport(RecordType.ECG, createTime, devAddress);		        
				
				if(reportJson == null) {
					ServletUtil.codeResponse(response, DOWNLOAD_ERR);
				} else {
					//System.out.println(reportJson.toString());
					ServletUtil.contentResponse(response, reportJson);
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
