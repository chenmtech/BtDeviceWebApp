package com.cmtech.web.servlet;

import static com.cmtech.web.btdevice.ReturnCode.DATA_ERR;
import static com.cmtech.web.btdevice.ReturnCode.INVALID_PARA_ERR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Servlet implementation class RecordServlet
 * 为手机端App提供记录操作的Servlet类
 * 注意：PC端App不需要这个类，它是通过直接连接数据库来操作记录的
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

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String sver = req.getParameter("sver");
		
		if(sver == null || sver.equals("1.0")) {
			WebCommandService10.doRecordGet(req, resp);
			return;
		}
		
		if(sver.equals("1.1")) {
			WebCommandService11.doRecordGet(req, resp);
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String charEncoding = req.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        
        JSONObject reqJson = null;
        try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(req.getInputStream(), charEncoding))) {
        	StringBuilder strBuilder = new StringBuilder();
			String s;
			while ((s = streamReader.readLine()) != null)
				strBuilder.append(s);
			reqJson = new JSONObject(strBuilder.toString());
        } catch (JSONException e) {
			e.printStackTrace();
			ServletUtil.codeResponse(resp, DATA_ERR, "数据错误");
			return;
		} 
        
        if(!reqJson.has("sver") || reqJson.getString("sver").equals("1.0")) {
			WebCommandService10.doRecordPost(reqJson, resp);
			return;
		}
        

		if(reqJson.getString("sver").equals("1.1")) {
			WebCommandService11.doRecordPost(reqJson, resp);
			return;
		}
		
		ServletUtil.codeResponse(resp, INVALID_PARA_ERR, "数据错误");   
	}
}
