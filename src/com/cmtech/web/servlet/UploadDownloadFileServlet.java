package com.cmtech.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * 实现信号数据文件的上传和下载
 * @author chenm
 *
 */
@WebServlet(name="UploadDownloadFileServlet", urlPatterns="/File")
public class UploadDownloadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletFileUpload uploader = null;
	private File sigPath = new File(System.getProperty("catalina.home")+File.separator + "DATA");
    
	@Override
	public void init() throws ServletException{
    	if(!sigPath.exists()) sigPath.mkdirs();
		DiskFileItemFactory fileFactory = new DiskFileItemFactory();
		fileFactory.setRepository(sigPath);
		this.uploader = new ServletFileUpload(fileFactory);
	}
	
	/**
	 * 用于实现文件下载服务
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sigType = request.getParameter("sigType"); 
		String fileName = request.getParameter("fileName"); 
		 if(fileName == null || fileName.equals("")){ 
			 throw new ServletException("File Name can't be null or empty"); 
		 } 
		 
		 File typePath = new File(sigPath, sigType); 
		 File file = new File(typePath, fileName);
		 if(!file.exists()){ 
			  throw new ServletException("File doesn't exists on server:" + file.getAbsolutePath());
		 }
		 //System.out.println("File location on server::"+file.getAbsolutePath());
		 
		ServletContext ctx = getServletContext();
		InputStream fis = new FileInputStream(file);
		String mimeType = ctx.getMimeType(file.getAbsolutePath());
		response.setContentType(mimeType != null? mimeType:"application/octet-stream");
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				
		ServletOutputStream os       = response.getOutputStream();
		byte[] bufferData = new byte[1024];
		int read=0;
		while((read = fis.read(bufferData))!= -1){
			os.write(bufferData, 0, read);
		}
		os.flush();
		os.close();
		fis.close();
		//System.out.println("File downloaded at client successfully");
	}

	/**
	 * 用于实现文件上传服务
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!ServletFileUpload.isMultipartContent(request)){
			throw new ServletException("Content type is not multipart/form-data");
		}
		
		File file = null;
		try {
			List<FileItem> fileItemsList = uploader.parseRequest(request);
			Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
			while(fileItemsIterator.hasNext()){
				FileItem fileItem = fileItemsIterator.next();
				//System.out.println("FieldName="+fileItem.getFieldName());
				//System.out.println("FileName="+fileItem.getName());
				//System.out.println("ContentType="+fileItem.getContentType());
				//System.out.println("Size in bytes="+fileItem.getSize());
				
				File typePath = new File(sigPath, fileItem.getFieldName());
				if(!typePath.exists()) typePath.mkdirs();
				
				file = new File(typePath, fileItem.getName());
				//System.out.println("Absolute Path at server="+file.getAbsolutePath());
				
				if(!file.exists()) {
					fileItem.write(file);
				}
			}
		} catch (Exception e) {
			if(file != null && file.exists())
				file.delete();
		}
	}

}
