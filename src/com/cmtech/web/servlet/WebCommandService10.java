package com.cmtech.web.servlet;

import static com.cmtech.web.MyConstant.INVALID_ID;
import static com.cmtech.web.MyConstant.INVALID_TIME;
import static com.cmtech.web.btdevice.ReturnCode.DATA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.INVALID_PARA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.SUCCESS;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.AppUpdateInfo;
import com.cmtech.web.btdevice.RecordType;

public class WebCommandService10{	
	//---------------------------------------------------------- 记录操作命令
	// 上传记录
	private static final String CMD_UPLOAD = "upload";
	
	// 下载记录
	private static final String CMD_DOWNLOAD = "download";
	
	// 删除记录
	private static final String CMD_DELETE = "delete";
	
	// 下载记录列表
	private static final String CMD_DOWNLOAD_RECORDS= "downloadRecords";
	
	// 获取记录诊断报告
	private static final String CMD_RETRIEVE_DIAGNOSE_REPORT = "retrieveDiagnoseReport";
	
	// 分享记录
	private static final String CMD_SHARE = "share";
	
	
	private WebCommandService10() {
		
	}
		
		
	public static void doAccountGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
				ServletUtil.codeResponse(resp, DATA_ERR);
			}
			return;
		}
		
		// 登录，如果成功，返回账户ID号
		if(cmd.equals("login")) {
			int id = Account.login(userName, password);
			
			if(id == INVALID_ID) {
				ServletUtil.codeResponse(resp, DATA_ERR);
			} else {
				JSONObject json = new JSONObject();
				json.put("id", id);
				ServletUtil.dataResponse(resp, json);
			}
			return;
		}
		
		// 修改密码
		if(cmd.equals("resetPassword")) {
			if(Account.resetPassword(userName, password)) {
				ServletUtil.codeResponse(resp, SUCCESS);
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR);
			}
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
		return;
	}

	public static void doAccountPost(JSONObject reqJson, HttpServletResponse resp) throws ServletException, IOException {
		// 验证账户是否有效
		int id = reqJson.getInt("id");
		if(!Account.isAccountValid(id)) {
			ServletUtil.codeResponse(resp, DATA_ERR);
			return;
		}
		
		/*
		Account account = new Account(id);
		
		String cmd = reqJson.getString("cmd");
		switch(cmd) {
		
		// 上传
		case "upload":
			account.fromJson(reqJson);
			if(account.update()) {
				ServletUtil.codeResponse(resp, SUCCESS);
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR);
			}
			break;
			
		// 下载	
		case "download":
			if(account.retrieve()) {
				ServletUtil.contentResponse(resp, account.toJson());
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR);
			}
			break;
			
		case "downloadShareInfo":
			List<ContactInfo> found = ContactInfo.retrieveContactInfo(id);
			if(found == null || found.isEmpty()) 
				ServletUtil.codeResponse(resp, SUCCESS);
			else {				
				JSONArray jsonArray = new JSONArray();
				for(ContactInfo shareInfo : found) {
					jsonArray.put(shareInfo.toJson());
				}
				ServletUtil.contentResponse(resp, jsonArray);
			}				
			break;
			
		case "changeShareInfo":
			int fromId = reqJson.getInt("fromId");
			int status = reqJson.getInt("status");
			if(ContactInfo.agree(fromId, id, status))
				ServletUtil.codeResponse(resp, SUCCESS);
			else
				ServletUtil.codeResponse(resp, DATA_ERR);
			break;
			
		case "addShare":
			int toId = reqJson.getInt("toId");
			if(Account.exist(toId) && ContactInfo.getId(id, toId) == INVALID_ID && ContactInfo.insert(id, toId))
				ServletUtil.codeResponse(resp, SUCCESS);
			else
				ServletUtil.codeResponse(resp, DATA_ERR);
			break;	
			
		case "downloadContactPerson":
			int contactId = reqJson.getInt("contactId");
			Account contact = new Account(contactId);
			if(contact.retrieve()) {
				ServletUtil.contentResponse(resp, contact.contactInfoToJson());
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR);
			}
			break;	
			
			default:
				ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
				break;
		}	
		*/	
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
	}	

	public static void doRecordGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String strRecordTypeCode = req.getParameter("recordTypeCode");
		String strAccountId = req.getParameter("accountId");
		String strCreateTime = req.getParameter("createTime");
		String devAddress = req.getParameter("devAddress");
		if(strRecordTypeCode == null || strAccountId == null || strCreateTime == null || devAddress == null) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
			return;
		}
		RecordType type = RecordType.fromCode( Integer.parseInt(strRecordTypeCode) );
		int accountId = Integer.parseInt(strAccountId);
		long createTime = Long.parseLong(strCreateTime);
		
		int id = RecordWebCommandService.getId(type, accountId, createTime, devAddress);
		JSONObject json = new JSONObject();
		json.put("id", id);
		ServletUtil.dataResponse(resp, json);
	}
	
	public static void doRecordPost(JSONObject reqJson, HttpServletResponse resp) throws ServletException, IOException {
		// 验证账户是否有效
		int accountId = reqJson.getInt("accountId");
		if(!Account.isAccountValid(accountId)) {
			ServletUtil.codeResponse(resp, DATA_ERR);
			return;
		}

		// 执行命令
		String cmd = reqJson.getString("cmd");
		RecordType type;			
		boolean cmdResult = false;
		long createTime = INVALID_TIME;
		String devAddress;
		
		switch(cmd) {
			case CMD_UPLOAD:
				type = RecordType.fromCode(reqJson.getInt("recordTypeCode"));
				cmdResult = RecordWebCommandService.upload(type, reqJson);
				if(cmdResult) {
					ServletUtil.codeResponse(resp, SUCCESS);
				} else {
					ServletUtil.codeResponse(resp, DATA_ERR);
				}
				break;
				
			case CMD_DOWNLOAD:
				type = RecordType.fromCode(reqJson.getInt("recordTypeCode"));
				createTime = reqJson.getLong("createTime");
				devAddress = reqJson.getString("devAddress");
				JSONObject json = RecordWebCommandService.download(type, accountId, createTime, devAddress);
				
				if(json == null) {
					ServletUtil.codeResponse(resp, DATA_ERR);
				} else {
					//System.out.println(json.toString());
					ServletUtil.dataResponse(resp, json);
				}
				break;
				
			case CMD_DELETE:
				type = RecordType.fromCode(reqJson.getInt("recordTypeCode"));
				createTime = reqJson.getLong("createTime");
				devAddress = reqJson.getString("devAddress");
				cmdResult = RecordWebCommandService.delete(type, accountId, createTime, devAddress);
				if(cmdResult) {
					ServletUtil.codeResponse(resp, SUCCESS);
				} else {
					ServletUtil.codeResponse(resp, DATA_ERR);
				}
				break;
				
			case CMD_DOWNLOAD_RECORDS:
				String typeStr = reqJson.getString("recordTypeCode");
				String[] typeStrArr = typeStr.split(",");
				RecordType[] types = new RecordType[typeStrArr.length];
				for(int i = 0; i < typeStrArr.length; i++) {
					types[i] = RecordType.fromCode(Integer.parseInt(typeStrArr[i]));
				}
				long fromTime = reqJson.getLong("fromTime");
				//int creatorId = inputJson.getInt("creatorId");
				int num = reqJson.getInt("num");
				String filterStr = reqJson.getString("filterStr");
				JSONArray jsonRecords = RecordWebCommandService.download(types, accountId, fromTime, filterStr, num);
				if(jsonRecords == null)
					ServletUtil.codeResponse(resp, SUCCESS);
				else
					ServletUtil.dataResponse(resp, jsonRecords);
				break;
				
			case CMD_RETRIEVE_DIAGNOSE_REPORT:
				createTime = reqJson.getLong("createTime");
		        devAddress = reqJson.getString("devAddress");
		        JSONObject reportJson = RecordWebCommandService.retrieveDiagnoseReport(RecordType.ECG, accountId, createTime, devAddress);		        
				
				if(reportJson == null) {
					ServletUtil.codeResponse(resp, DATA_ERR);
				} else {
					//System.out.println(reportJson.toString());
					ServletUtil.dataResponse(resp, reportJson);
				}
				break;
					
			case CMD_SHARE:
				type = RecordType.fromCode(reqJson.getInt("recordTypeCode"));
				createTime = reqJson.getLong("createTime");
		        devAddress = reqJson.getString("devAddress");
		        int shareId = reqJson.getInt("shareId");
		        
		        cmdResult = RecordWebCommandService.share(type, accountId, createTime, devAddress, shareId);
		        if(cmdResult)
	        		ServletUtil.codeResponse(resp, SUCCESS);
	        	else
	        		ServletUtil.codeResponse(resp, DATA_ERR);
		        break;     
	
				
			default:
				ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
				break;	        	
		}
	}
	
	public static void doAppUpdateGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String cmd = req.getParameter("cmd");
		if(!"download".equals(cmd)) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
		} else {
			AppUpdateInfo updateInfo = new AppUpdateInfo();
			if(updateInfo.retrieve()) {
				ServletUtil.dataResponse(resp, updateInfo.toJson());
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR);
			}
		}
	}
	
	public static void doAppUpdatePost(JSONObject reqJson, HttpServletResponse resp) throws ServletException, IOException {
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR);
	}
}
