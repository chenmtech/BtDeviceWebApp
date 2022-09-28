package com.cmtech.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * 实现文件的上传和下载
 * @author chenm
 *
 */
@WebServlet(name="UploadDownloadFileServlet", urlPatterns="/File")
public class UploadDownloadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// 文件上传器
	private ServletFileUpload uploader = null;
	
	// 存放文件根路径
	private final File rootPath = new File(System.getProperty("catalina.home")+File.separator + "DATA");
    
	@Override
	public void init() throws ServletException{
    	if(!rootPath.exists()) rootPath.mkdirs();
		DiskFileItemFactory fileFactory = new DiskFileItemFactory();
		fileFactory.setRepository(rootPath);
		this.uploader = new ServletFileUpload(fileFactory);
	}
	
	/**
	 * 用于实现文件下载或者判断文件是否存在
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cmd = request.getParameter("cmd");
		// 信号类型参数，决定了要下载的文件从根目录的哪个子目录中去寻找。比如type="ECG"，则从rootPath/ECG目录中寻找文件
		String sigType = request.getParameter("sigType"); 
		String fileName = request.getParameter("fileName"); 
		if(fileName == null || fileName.equals("")){ 
			throw new ServletException("File Name can't be null or empty"); 
		} 
		 
		File typePath = new File(rootPath, sigType); 
		File file = new File(typePath, fileName);
		if(!file.exists()){ 
			 throw new ServletException("The file doesn't exist at server path:" + file.getAbsolutePath());
		}
		
		if(cmd.equals("find")) {
			return;
		} else if(cmd.equals("download")) {		 
			ServletContext ctx = getServletContext();
			String mimeType = ctx.getMimeType(file.getAbsolutePath());
			response.setContentType(mimeType != null? mimeType:"application/octet-stream");
			response.setContentLength((int) file.length());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	
			InputStream fis = new FileInputStream(file);
			ServletOutputStream os = response.getOutputStream();
			byte[] bufferData = new byte[1024];
			int read=0;
			while((read = fis.read(bufferData))!= -1){
				os.write(bufferData, 0, read);
			}
			os.flush();
			os.close();
			fis.close();
		} else {
			throw new IOException("The command is wrong.");
		}
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
			for(FileItem fileItem : fileItemsList) {
				String sigType = fileItem.getFieldName();
				String fileName = fileItem.getName();
				File typePath = new File(rootPath, sigType);
				if(!typePath.exists()) typePath.mkdirs();				
				file = new File(typePath, fileName);
				
				fileItem.write(file); // 写信号文件
				System.out.println("Success writing the file:" + file.getAbsolutePath());
			}			
		} catch (Exception e) {
			throw new IOException("error writing the data into file.");
		}
	}

}
