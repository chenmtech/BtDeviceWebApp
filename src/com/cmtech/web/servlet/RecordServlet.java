package com.cmtech.web.servlet;

import static com.cmtech.web.exception.MyExceptionCode.ACCOUNT_ERR;
import static com.cmtech.web.exception.MyExceptionCode.INVALID_PARA_ERR;
import static com.cmtech.web.exception.MyExceptionCode.NO_ERR;
import static com.cmtech.web.exception.MyExceptionCode.UPDATE_ERR;
import static com.cmtech.web.exception.MyExceptionCode.UPLOAD_ERR;
import static com.cmtech.web.exception.MyExceptionCode.DOWNLOAD_ERR;
import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.Account;
import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.exception.MyException;
import com.cmtech.web.util.MyServletUtil;
import com.cmtech.web.util.RecordUtil;


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
		
		int id = RecordUtil.queryRecord(type, createTime, devAddress);
		
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
			if(platName == null || platId == null) {
				response(response, new MyException(INVALID_PARA_ERR, "无效参数"));
				return;
			}
			int accountId = Account.getId(platName, platId);
			if(accountId == INVALID_ID) {
				response(response, new MyException(ACCOUNT_ERR, "无效用户"));
				return;
			}			

			String cmd = jsonObject.getString("cmd");
			RecordType type = RecordType.getType(jsonObject.getInt("recordTypeCode"));
			long createTime = jsonObject.getLong("createTime");
			String devAddress = jsonObject.getString("devAddress");
			if(cmd.equals("updateNote")) {
				String note = jsonObject.getString("note");
				boolean rlt = RecordUtil.updateNote(type, createTime, devAddress, note);
				if(rlt) {
					response(response, new MyException(NO_ERR, "更新成功"));
				} else {
					response(response, new MyException(UPDATE_ERR, "更新失败"));
				}
				return;
			}
			
			else if(cmd.equals("upload")) {
				String verStr = jsonObject.getString("ver");
				String[] verStrs = verStr.split(",");
				byte[] ver = new byte[] {Byte.parseByte(verStrs[0]), Byte.parseByte(verStrs[1])};
				String creatorPlat = jsonObject.getString("creatorPlat");
				String creatorId = jsonObject.getString("creatorId");
				int sampleRate = jsonObject.getInt("sampleRate");
				int caliValue = jsonObject.getInt("caliValue");
				int leadTypeCode = jsonObject.getInt("leadTypeCode");
				int recordSecond = jsonObject.getInt("recordSecond");
				String note = jsonObject.getString("note");
				String ecgData = jsonObject.getString("ecgData");

				BleEcgRecord10 record = new BleEcgRecord10();
				record.setVer(ver);
				record.setCreateTime(createTime);
				record.setDevAddress(devAddress);
				record.setCreator(new Account(creatorPlat, creatorId));
				record.setSampleRate(sampleRate);
				record.setCaliValue(caliValue);
				record.setLeadTypeCode(leadTypeCode);
				record.setRecordSecond(recordSecond);
				record.setNote(note);
				record.setEcgData(ecgData);
				
				boolean rlt = RecordUtil.upload(record);
				if(rlt) {
					response(response, new MyException(NO_ERR, "上传成功"));
				} else {
					response(response, new MyException(UPLOAD_ERR, "上传失败"));
				}
				return;
			}
			
			else if(cmd.equals("download")) {
				int id = RecordUtil.queryRecord(type, createTime, devAddress);
				if(id == INVALID_ID) {
					response(response, new MyException(DOWNLOAD_ERR, "下载记录错误"));
					return;
				} else {
					JSONObject json = RecordUtil.getRecordToJson(id);
					MyServletUtil.responseWithJson(response, json);
					return;
				}
			}
			
			else {
				response(response, new MyException(INVALID_PARA_ERR, "无效参数"));
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
