package com.cmtech.web.servlet;

import static com.cmtech.web.exception.MyExceptionCode.ACCOUNT_ERR;
import static com.cmtech.web.exception.MyExceptionCode.DOWNLOAD_ERR;
import static com.cmtech.web.exception.MyExceptionCode.INVALID_PARA_ERR;
import static com.cmtech.web.exception.MyExceptionCode.NO_ERR;
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
import com.cmtech.web.util.MyServletUtil;
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
		MyServletUtil.responseWithJson(response, json);
		
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
			String cmd = jsonObject.getString("cmd");
			
			if(platName == null || platId == null) {
				response(response, new MyException(INVALID_PARA_ERR, "无效参数"));
				return;
			}
			int accountId = Account.getId(platName, platId);
			if(accountId == INVALID_ID) {
				response(response, new MyException(ACCOUNT_ERR, "无效用户"));
				return;
			}			

			RecordType type = RecordType.getType(jsonObject.getInt("recordTypeCode"));
			if(cmd.equals("updateNote")) { // update the note of a record
				long createTime = jsonObject.getLong("createTime");
				String devAddress = jsonObject.getString("devAddress");
				String note = jsonObject.getString("note");
				boolean rlt = RecordDbUtil.updateNote(type, createTime, devAddress, note);
				if(rlt) {
					response(response, new MyException(NO_ERR, "更新成功"));
				} else {
					response(response, new MyException(UPDATE_ERR, "更新失败"));
				}
				return;
			}
			
			else if(cmd.equals("upload")) { // upload one record
				boolean rlt = RecordDbUtil.upload(type, jsonObject);
				if(rlt) {
					response(response, new MyException(NO_ERR, "上传成功"));
				} else {
					response(response, new MyException(UPLOAD_ERR, "上传失败"));
				}
				return;
			}
			
			else if(cmd.equals("download")) { // download some records
				long fromTime = jsonObject.getLong("fromTime");
				String creatorPlat = jsonObject.getString("creatorPlat");
				String creatorId = jsonObject.getString("creatorId");
				int num = jsonObject.getInt("num");
				JSONArray jsonArr = RecordDbUtil.download(type, creatorPlat, creatorId, fromTime, num);
				
				if(jsonArr == null) {
					response(response, new MyException(DOWNLOAD_ERR, "下载记录错误"));
					return;
				} else {
					System.out.println(jsonArr.toString());
					JSONObject json = new JSONObject();
					json.put("code", NO_ERR.ordinal());
					json.put("errStr", "下载记录成功");
					json.put("records", jsonArr);
					MyServletUtil.responseWithJson(response, json);
					return;
				}
			}
			
			else if(cmd.equals("delete")) { // delete one record
				long createTime = jsonObject.getLong("createTime");
				String devAddress = jsonObject.getString("devAddress");
				boolean rlt = RecordDbUtil.delete(type, createTime, devAddress);
				if(rlt) {
					response(response, new MyException(NO_ERR, "删除成功"));
				} else {
					response(response, new MyException(UPDATE_ERR, "删除失败"));
				}
				return;
			}
			
			else {
				response(response, new MyException(INVALID_PARA_ERR, "无效命令"));
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			streamReader.close();
		}
	}

	private void response(HttpServletResponse resp, MyException exception) {
		JSONObject json = new JSONObject();
		json.put("code", exception.getCode().ordinal());
		json.put("errStr", exception.getDescription());
		
		MyServletUtil.responseWithJson(resp, json);
	}
}
