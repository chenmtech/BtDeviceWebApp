package com.cmtech.web.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.dbop.Account;
import com.cmtech.web.util.MySQLUtil;

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
		// TODO Auto-generated method stub
		
		MySQLUtil.connect();
		BleEcgRecord10 record = new BleEcgRecord10();
		record.setVer(new byte[] {0x01,0x00});
		record.setCreateTime(new Date().getTime());
		record.setDevAddress("12:34:56:78");
		record.setCreator(new Account("chenm", "ctl080512"));
		record.setSampleRate(125);
		record.setCaliValue(164);
		if(record.insert()) {
			System.out.println("插入记录成功,id="+record.getId());
		} else {
			System.out.println("插入记录失败");
		}
		MySQLUtil.disconnect();
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
