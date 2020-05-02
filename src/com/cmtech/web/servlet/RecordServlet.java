package com.cmtech.web.servlet;

import static com.cmtech.web.exception.MyExceptionCode.ACCOUNT_ERR;
import static com.cmtech.web.exception.MyExceptionCode.DOWNLOAD_ERR;
import static com.cmtech.web.exception.MyExceptionCode.INVALID_PARA_ERR;
import static com.cmtech.web.exception.MyExceptionCode.SUCCESS;
import static com.cmtech.web.exception.MyExceptionCode.UPDATE_ERR;
import static com.cmtech.web.exception.MyExceptionCode.UPLOAD_ERR;
import static com.cmtech.web.util.DbUtil.INVALID_ID;

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
import com.cmtech.web.exception.MyException;
import com.cmtech.web.util.ServletUtil;
import com.cmtech.web.util.RecordDbUtil;


/**
 * Servlet implementation class RecordUploadServlet
 */
@WebServlet(name="RecordServlet", urlPatterns="/Record")
public class RecordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecordServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// query the record using recordTypeCode, createTime and devAddress
		// return the id of the record with json
		// if not exist, return INVALID_ID
		String strRecordTypeCode = request.getParameter("recordTypeCode");
		int recordTypeCode = Integer.parseInt(strRecordTypeCode);
		RecordType type = RecordType.getType(recordTypeCode);
		String strCreateTime = request.getParameter("createTime");
		long createTime = Long.parseLong(strCreateTime);
		String devAddress = request.getParameter("devAddress");
		
		int id = RecordDbUtil.query(type, createTime, devAddress);
		
		JSONObject json = new JSONObject();
		json.put("id", id);
		ServletUtil.responseJson(response, json);
		
		System.out.println("记录id="+id);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// upload one record with json 
		// update the note of a record with json 
		// return the result with json
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
				ServletUtil.response(response, new MyException(INVALID_PARA_ERR, "无效参数"));
				return;
			}
			int accountId = Account.getId(platName, platId);
			if(accountId == INVALID_ID) {
				ServletUtil.response(response, new MyException(ACCOUNT_ERR, "无效用户"));
				return;
			}			

			String cmd = jsonObject.getString("cmd");
			RecordType type = RecordType.getType(jsonObject.getInt("recordTypeCode"));
			boolean rlt;
			long createTime;
			String devAddress;
			switch(cmd) {
			case "updateNote":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				String note = jsonObject.getString("note");
				rlt = RecordDbUtil.updateNote(type, createTime, devAddress, note);
				if(rlt) {
					ServletUtil.response(response, new MyException(SUCCESS, "更新成功"));
				} else {
					ServletUtil.response(response, new MyException(UPDATE_ERR, "更新失败"));
				}
				break;
				
			case "upload":
				rlt = RecordDbUtil.upload(type, jsonObject);
				if(rlt) {
					ServletUtil.response(response, new MyException(SUCCESS, "上传成功"));
				} else {
					ServletUtil.response(response, new MyException(UPLOAD_ERR, "上传失败"));
				}
				break;
				
			case "downloadInfo":
				long fromTime = jsonObject.getLong("fromTime");
				String creatorPlat = jsonObject.getString("creatorPlat");
				String creatorId = jsonObject.getString("creatorId");
				int num = jsonObject.getInt("num");
				JSONArray jsonArr = RecordDbUtil.downloadInfo(type, creatorPlat, creatorId, fromTime, num);
				
				if(jsonArr == null) {
					ServletUtil.response(response, new MyException(DOWNLOAD_ERR, "下载记录信息错误"));
				} else {
					System.out.println(jsonArr.toString());
					JSONObject json = new JSONObject();
					json.put("code", SUCCESS.ordinal());
					json.put("errStr", "下载记录信息成功");
					json.put("records", jsonArr);
					ServletUtil.responseJson(response, json);
				}
				break;
				
			case "download":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				JSONObject json = RecordDbUtil.download(type, createTime, devAddress);
				
				if(json == null) {
					ServletUtil.response(response, new MyException(DOWNLOAD_ERR, "下载记录错误"));
				} else {
					System.out.println(json.toString());
					JSONObject json1 = new JSONObject();
					json1.put("code", SUCCESS.ordinal());
					json1.put("errStr", "下载记录成功");
					json1.put("record", json);
					ServletUtil.responseJson(response, json1);
				}
				break;
				
			case "delete":
				createTime = jsonObject.getLong("createTime");
				devAddress = jsonObject.getString("devAddress");
				rlt = RecordDbUtil.delete(type, createTime, devAddress);
				if(rlt) {
					ServletUtil.response(response, new MyException(SUCCESS, "删除成功"));
				} else {
					ServletUtil.response(response, new MyException(UPDATE_ERR, "删除失败"));
				}
				break;
				
				default:
					ServletUtil.response(response, new MyException(INVALID_PARA_ERR, "无效命令"));
					break;				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			streamReader.close();
		}
	}
}
