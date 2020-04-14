package com.cmtech.web.servlet;

import static com.cmtech.web.util.MySQLUtil.INVALID_ID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.dbop.Account;
import com.cmtech.web.util.MyServletUtil;


/**
 * Servlet implementation class RecordUploadServlet
 */
@WebServlet(name="RecordUploadServlet", urlPatterns="/RecordUpload")
public class RecordUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecordUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// find the record using recordTypeCode, createTime and devAddress
		// return the id of the record with json
		// if not exist, return INVALID_ID
		String strRecordTypeCode = request.getParameter("recordTypeCode");
		int recordTypeCode = Integer.parseInt(strRecordTypeCode);
		RecordType type = RecordType.getType(recordTypeCode);
		String strCreateTime = request.getParameter("createTime");
		long createTime = Long.parseLong(strCreateTime);
		String devAddress = request.getParameter("devAddress");
		
		int id = INVALID_ID;
		switch(type) {
		case ECG:
			id = BleEcgRecord10.getId(createTime, devAddress);
			break;
		default:
			break;
		}
		
		Map<String, String> data = new HashMap<>();
		data.put("id", String.valueOf(id));
		MyServletUtil.responseWithJson(response, data);
		
		System.out.println("记录id="+id);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// receive the uploaded record with json 
		// response isSuccess and errStr with json
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
			
			int recordId = INVALID_ID;
			
			String platName = jsonObject.getString("platName");
			String platId = jsonObject.getString("platId");
			if(platName == null || platId == null) {
				response(response, false, "上传无效参数");
				return;
			}
			int accountId = Account.getId(platName, platId);
			if(accountId == INVALID_ID) {
				System.out.println("用户未注册");
				response(response, false, "用户未注册");
				return;
			}
			
			RecordType type = RecordType.getType(jsonObject.getInt("recordTypeCode"));
			long createTime = jsonObject.getLong("createTime");
			String devAddress = jsonObject.getString("devAddress");
			if(type != RecordType.ECG) {
				System.out.println("记录类型不支持");
				response(response, false, "记录类型不支持");
				return;
			}
			recordId = BleEcgRecord10.getId(createTime, devAddress);
			if(recordId != INVALID_ID) {
				System.out.println("记录已存在");
				String note = jsonObject.getString("note");
				BleEcgRecord10.updateNote(recordId, note);
				response(response, true, "记录已更新");
				return;
			}
			
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
			if(record.insert()) {
				recordId = record.getId();
				System.out.println("上传记录成功,id="+recordId);
				response(response, true, "记录上传成功");
			} else {
				System.out.println("上传记录失败");
				response(response, false, "记录上传失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			streamReader.close();
		}
	}

	private void response(HttpServletResponse resp, boolean isSuccess, String errStr) {
		Map<String, String> data = new HashMap<>();
		data.put("isSuccess", String.valueOf(isSuccess));
		data.put("errStr", errStr);
		
		MyServletUtil.responseWithJson(resp, data);
	}
}
