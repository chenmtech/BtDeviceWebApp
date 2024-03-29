package com.cmtech.web.servlet;

import static com.cmtech.web.MyConstant.INVALID_ID;
import static com.cmtech.web.MyConstant.INVALID_TIME;
import static com.cmtech.web.btdevice.ReturnCode.DATA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.INVALID_PARA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.SUCCESS;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.AppUpdateInfo;
import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.btdevice.ContactInfo;

public class WebCommandService11{	
	// 查询一条记录的ID
    public static final int CMD_QUERY_RECORD_ID = 1;    
    // 上传一条记录
    public static final int CMD_UPLOAD_RECORD = 2;    
    // 下载一条记录
    public static final int CMD_DOWNLOAD_RECORD = 3;
    // 删除一条记录
    public static final int CMD_DELETE_RECORD = 4;
    // 下载多条记录
    public static final int CMD_DOWNLOAD_RECORDS = 5;
    // 分享一条记录
    public static final int CMD_SHARE_RECORD = 6;
    // 获取一条记录的诊断报告
    public static final int CMD_RETRIEVE_DIAGNOSE_REPORT = 7;
    // 上传账户信息
    public static final int CMD_UPLOAD_ACCOUNT = 8; 
    // 下载账户信息
    public static final int CMD_DOWNLOAD_ACCOUNT = 9;
    // 注册账户
    public static final int CMD_SIGNUP = 10;
    // 登录账户
    public static final int CMD_LOGIN = 11;
    // 重置账户密码
    public static final int CMD_RESET_PASSWORD = 12;
    // 下载账户联系人信息，仅包括发起者ID，接收者ID，以及申请状态
    public static final int CMD_DOWNLOAD_CONTACT_INFO = 13;
    // 下载联系人的账户信息，包括账户ID，昵称，简介和头像
    public static final int CMD_DOWNLOAD_CONTACT_ACCOUNT_INFO = 14;
    // 添加一条申请联系人信息
    public static final int CMD_ADD_CONTACT = 15;
    // 同意一条账户联系人申请信息
    public static final int CMD_AGREE_CONTACT = 16;
    // 删除一条联系人信息
    public static final int CMD_DELETE_CONTACT = 17;
    // 下载APP更新信息
    public static final int CMD_DOWNLOAD_APP_INFO = 18;
    // 下载APK文件
    public static final int CMD_DOWNLOAD_APK = 19;
	
	
	private WebCommandService11() {
		
	}
		
		
	public static void doAccountGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String cmdStr = req.getParameter("cmd");
		
		if(cmdStr == null) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
			return;
		}
		
		int cmd = Integer.parseInt(cmdStr);
		String userName = req.getParameter("userName");
		String password = req.getParameter("password");
		if(userName == null || password == null) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
			return;
		}
		
		// 注册新用户
		switch(cmd) {
		case CMD_SIGNUP:			
			if(Account.signUp(userName, password)) {
				ServletUtil.codeResponse(resp, SUCCESS, "注册成功");
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR, "注册错误");
			}
			return;
		
		// 登录，如果成功，返回账户ID号
		case CMD_LOGIN:
			int id = Account.login(userName, password);
			
			if(id == INVALID_ID) {
				ServletUtil.codeResponse(resp, DATA_ERR, "登录错误");
			} else {
				JSONObject json = new JSONObject();
				json.put("id", id);
				ServletUtil.dataResponse(resp, json);
			}
			return;
		
		// 重置密码
		case CMD_RESET_PASSWORD:
			if(Account.resetPassword(userName, password)) {
				ServletUtil.codeResponse(resp, SUCCESS, "重置成功");
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR, "重置错误");
			}
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
		return;
	}

	public static void doAccountPost(JSONObject reqJson, HttpServletResponse resp) throws ServletException, IOException {
		// 验证账户是否有效
		int accountId = reqJson.getInt("accountId");
		if(!Account.isAccountValid(accountId)) {
			ServletUtil.codeResponse(resp, DATA_ERR, "数据错误");
			return;
		}
		
		Account account = new Account(accountId);
		
		int cmd = reqJson.getInt("cmd");
		switch(cmd) {
		
		// 上传
		case CMD_UPLOAD_ACCOUNT:
			account.fromJson(reqJson);
			if(account.update()) {
				ServletUtil.codeResponse(resp, SUCCESS, "更新成功");
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR, "更新错误");
			}
			break;
			
		// 下载	
		case CMD_DOWNLOAD_ACCOUNT:
			if(account.retrieve()) {
				ServletUtil.dataResponse(resp, account.toJson());
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR, "下载错误");
			}
			break;	

		// 下载联系人信息
		case CMD_DOWNLOAD_CONTACT_INFO:
			List<ContactInfo> found = ContactInfo.retrieveContactInfo(accountId);
			if(found == null || found.isEmpty()) 
				ServletUtil.codeResponse(resp, SUCCESS, "下载成功");
			else {				
				JSONArray jsonArray = new JSONArray();
				for(ContactInfo ci : found) {
					jsonArray.put(ci.toJson());
				}
				ServletUtil.dataResponse(resp, jsonArray);
			}				
			break;
			
		// 下载多个联系人的账户信息
		case CMD_DOWNLOAD_CONTACT_ACCOUNT_INFO:
			JSONArray jsonArray = new JSONArray();
			String contactIdsStr = reqJson.getString("contactIds");
			String[] strs = contactIdsStr.split(",");
			for(String s : strs) {
				int contactId = Integer.valueOf(s);
				Account contact = new Account(contactId);
				if(contact.retrieve()) {
					jsonArray.put(contact.contactInfoToJson());
				}
			}
			ServletUtil.dataResponse(resp, jsonArray);
			break;	
			
		case CMD_ADD_CONTACT:
			int addId = reqJson.getInt("contactId");
			if(Account.exist(addId) && ContactInfo.getId(accountId, addId) == INVALID_ID && ContactInfo.insert(accountId, addId))
				ServletUtil.codeResponse(resp, SUCCESS, "申请成功");
			else
				ServletUtil.codeResponse(resp, DATA_ERR, "申请错误");
			break;	
			
		case CMD_AGREE_CONTACT:
			int agreeId = reqJson.getInt("contactId");
			if(ContactInfo.agree(agreeId, accountId))
				ServletUtil.codeResponse(resp, SUCCESS, "建立联系成功");
			else
				ServletUtil.codeResponse(resp, DATA_ERR,  "建立联系失败");
			break;
			
		case CMD_DELETE_CONTACT:
			int contactId = reqJson.getInt("contactId");
			if(ContactInfo.delete(accountId, contactId))
				ServletUtil.codeResponse(resp, SUCCESS, "删除成功");
			else
				ServletUtil.codeResponse(resp, DATA_ERR,  "删除错误");
			break;
			
			default:
				ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
				break;
		}		
	}	

	public static void doRecordGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String strRecordTypeCode = req.getParameter("recordTypeCode");
		String strAccountId = req.getParameter("accountId");
		String strCreateTime = req.getParameter("createTime");
		String devAddress = req.getParameter("devAddress");
		if(strRecordTypeCode == null || strAccountId == null || strCreateTime == null || devAddress == null) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
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
			ServletUtil.codeResponse(resp, DATA_ERR, "数据错误");
			return;
		}

		// 执行命令
		int cmd = reqJson.getInt("cmd");
		RecordType type;			
		boolean cmdResult = false;
		long createTime = INVALID_TIME;
		String devAddress;
		
		switch(cmd) {
			case CMD_UPLOAD_RECORD:
				type = RecordType.fromCode(reqJson.getInt("recordTypeCode"));
				cmdResult = RecordWebCommandService.upload(type, reqJson);
				if(cmdResult) {
					ServletUtil.codeResponse(resp, SUCCESS, "上传成功");
				} else {
					ServletUtil.codeResponse(resp, DATA_ERR, "上传错误");
				}
				break;
				
			case CMD_DOWNLOAD_RECORD:
				type = RecordType.fromCode(reqJson.getInt("recordTypeCode"));
				createTime = reqJson.getLong("createTime");
				devAddress = reqJson.getString("devAddress");
				JSONObject json = RecordWebCommandService.download(type, accountId, createTime, devAddress);
				
				if(json == null) {
					ServletUtil.codeResponse(resp, DATA_ERR, "下载错误");
				} else {
					ServletUtil.dataResponse(resp, "下载成功", json);
				}
				break;
				
			case CMD_DELETE_RECORD:
				type = RecordType.fromCode(reqJson.getInt("recordTypeCode"));
				createTime = reqJson.getLong("createTime");
				devAddress = reqJson.getString("devAddress");
				cmdResult = RecordWebCommandService.delete(type, accountId, createTime, devAddress);
				if(cmdResult) {
					ServletUtil.codeResponse(resp, SUCCESS, "删除成功");
				} else {
					ServletUtil.codeResponse(resp, DATA_ERR, "删除记录错误");
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
				ServletUtil.dataResponse(resp, jsonRecords);
				break;
				
			case CMD_RETRIEVE_DIAGNOSE_REPORT:
				createTime = reqJson.getLong("createTime");
		        devAddress = reqJson.getString("devAddress");
		        JSONObject reportJson = RecordWebCommandService.retrieveDiagnoseReport(RecordType.ECG, accountId, createTime, devAddress);		        
				
				if(reportJson == null) {
					ServletUtil.codeResponse(resp, DATA_ERR, "获取错误");
				} else {
					ServletUtil.dataResponse(resp, reportJson);
				}
				break;
					
			case CMD_SHARE_RECORD:
				type = RecordType.fromCode(reqJson.getInt("recordTypeCode"));
				createTime = reqJson.getLong("createTime");
		        devAddress = reqJson.getString("devAddress");
		        int contactId = reqJson.getInt("contactId");
		        
		        cmdResult = RecordWebCommandService.share(type, accountId, createTime, devAddress, contactId);
		        if(cmdResult)
	        		ServletUtil.codeResponse(resp, SUCCESS, "分享成功");
	        	else
	        		ServletUtil.codeResponse(resp, DATA_ERR, "分享错误");
		        break;     
	
				
			default:
				ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
				break;	        	
		}
	}
	
	public static void doAppUpdateGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String cmdStr = req.getParameter("cmd");
		
		if(cmdStr == null) {
			ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
			return;
		}
		
		int cmd = Integer.parseInt(cmdStr);

		if(cmd == CMD_DOWNLOAD_APP_INFO ) {
			AppUpdateInfo updateInfo = new AppUpdateInfo();
			if(updateInfo.retrieve()) {
				ServletUtil.dataResponse(resp, updateInfo.toJson());
			} else {
				ServletUtil.codeResponse(resp, DATA_ERR, "获取错误");
			}
			return;
		}
		
		if(cmd == CMD_DOWNLOAD_APK) {
			ServletUtil.codeResponse(resp, DATA_ERR, "获取错误");
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
		return;
	}
	
	public static void doAppUpdatePost(JSONObject reqJson, HttpServletResponse resp) throws ServletException, IOException {
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
	}
}
