package com.cmtech.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet(name="UploadDownloadFileServlet", urlPatterns="/File")
public class UploadDownloadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletFileUpload uploader = null;
	private File path = new File(System.getProperty("catalina.home")+File.separator + "DATA");
    
	@Override
	public void init() throws ServletException{
    	if(!path.exists()) path.mkdirs();
		DiskFileItemFactory fileFactory = new DiskFileItemFactory();
		fileFactory.setRepository(path);
		this.uploader = new ServletFileUpload(fileFactory);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 String fileName = request.getParameter("fileName"); 
		 if(fileName == null || fileName.equals("")){ 
			 throw new ServletException("File Name can't be null or empty"); 
		 } 
		 File typePath = new File(path, "ECG"); 
		 File file = new File(typePath, fileName);
		 if(!file.exists()){ 
			  throw new ServletException("File doesn't exists on server:" + file.getAbsolutePath());
		 }
		 System.out.println("File location on server::"+file.getAbsolutePath());
		 
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
		System.out.println("File downloaded at client successfully");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!ServletFileUpload.isMultipartContent(request)){
			throw new ServletException("Content type is not multipart/form-data");
		}
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.write("<html><head></head><body>");
		try {
			List<FileItem> fileItemsList = uploader.parseRequest(request);
			Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
			while(fileItemsIterator.hasNext()){
				FileItem fileItem = fileItemsIterator.next();
				System.out.println("FieldName="+fileItem.getFieldName());
				System.out.println("FileName="+fileItem.getName());
				System.out.println("ContentType="+fileItem.getContentType());
				System.out.println("Size in bytes="+fileItem.getSize());
				
				File typePath = new File(path, fileItem.getFieldName());
				if(!typePath.exists()) typePath.mkdirs();
				File file = new File(typePath, fileItem.getName());
				if(file.exists()) file.delete();
				System.out.println("Absolute Path at server="+file.getAbsolutePath());
				fileItem.write(file);
				out.write("File "+fileItem.getName()+ " uploaded successfully.");
				out.write("<br>");
				out.write("<a href=\"File?fileName="+fileItem.getName()+"\">Download "+file.getAbsolutePath()+"</a>");
			}
		} catch (FileUploadException e) {
			out.write("FileUploadException in uploading file.");
		} catch (Exception e) {
			out.write("Exception in uploading file." + e);
		}
		out.write("</body></html>");
	}

}
