package com.cmtech.web.servlet;

import static com.cmtech.web.dbUtil.DbUtil.INVALID_ID;
import static com.cmtech.web.exception.MyExceptionCode.*;

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
import com.cmtech.web.dbUtil.RecordDbUtil;
import com.cmtech.web.exception.MyException;


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
			ServletUtil.responseException(response, new MyException(INVALID_PARA_ERR, "无效参数"));
			return;
		}
		RecordType type = RecordType.fromCode( Integer.parseInt(strRecordTypeCode) );
		long createTime = Long.parseLong(strCreateTime);
		
		int id = RecordDbUtil.query(type, createTime, devAddress);
		JSONObject json = new JSONObject();
		json.put("code", SUCCESS.ordinal());
		json.put("id", id);
		ServletUtil.responseJson(response, json);
		
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
				ServletUtil.responseException(response, new MyException(INVALID_PARA_ERR, "无效参数"));
				return;
			}
			if(Account.getId(platName, platId) == INVALID_ID) {
				ServletUtil.responseException(response, new MyException(ACCOUNT_ERR, "无效用户"));
				return;
			}			

			// 执行命令
			String cmd = jsonObject.getString("cmd");
			RecordType type = RecordType.fromCode(jsonObject.getInt("recordTypeCode"));
			boolean cmdResult = false;
			JSONObject jsonResult = null;
			long createTime;
			String devAddress;
			switch(cmd) {
			
			case "upload":
				cmdResult = RecordDbUtil.upload(type, jsonObject);
				if(cmdResult) {
					ServletUtil.responseException(response, new MyException(SUCCESS, "上传成功"));
				} else {
					ServletUtil.responseException(response, new MyException(UPLOAD_ERR, "上传失败"));
				}
				break;
				
			case "updateNote":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				String note = jsonObject.getString("note");
				cmdResult = RecordDbUtil.updateNote(type, createTime, devAddress, note);
				if(cmdResult) {
					ServletUtil.responseException(response, new MyException(SUCCESS, "更新成功"));
				} else {
					ServletUtil.responseException(response, new MyException(UPDATE_ERR, "更新失败"));
				}
				break;
				
			case "downloadBasicInfo":
				long fromTime = jsonObject.getLong("fromTime");
				String creatorPlat = jsonObject.getString("creatorPlat");
				String creatorId = jsonObject.getString("creatorId");
				int num = jsonObject.getInt("num");
				String noteSearchStr = jsonObject.getString("noteSearchStr");
				JSONArray jsonArr = RecordDbUtil.downloadBasicInfo(type, creatorPlat, creatorId, fromTime, noteSearchStr, num);
				
				if(jsonArr == null) {
					ServletUtil.responseException(response, new MyException(DOWNLOAD_ERR, "下载失败"));
				} else {
					System.out.println(jsonArr.toString());
					jsonResult = new JSONObject();
					jsonResult.put("code", SUCCESS.ordinal());
					jsonResult.put("records", jsonArr);
					ServletUtil.responseJson(response, jsonResult);
				}
				break;
				
			case "download":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				JSONObject json = RecordDbUtil.download(type, createTime, devAddress);
				
				if(json == null) {
					ServletUtil.responseException(response, new MyException(DOWNLOAD_ERR, "下载失败"));
				} else {
					System.out.println(json.toString());
					jsonResult = new JSONObject();
					jsonResult.put("code", SUCCESS.ordinal());
					jsonResult.put("record", json);
					ServletUtil.responseJson(response, jsonResult);
				}
				break;
				
			case "delete":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				cmdResult = RecordDbUtil.delete(type, createTime, devAddress);
				if(cmdResult) {
					ServletUtil.responseException(response, new MyException(SUCCESS, "删除成功"));
				} else {
					ServletUtil.responseException(response, new MyException(DELETE_ERR, "删除失败"));
				}
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
