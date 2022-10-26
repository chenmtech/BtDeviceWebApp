package com.cmtech.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
		
	// 存放文件根路径
	private final File rootPath = new File(System.getProperty("catalina.home")+File.separator + "MY_FILE");
	
	private static final String[] SUPPORT_FILE_TYPE = {"ECG", "EEG", "PPG", "PTT", "PIC"}; 
    
	@Override
	public void init() throws ServletException{
    	if(!rootPath.exists()) rootPath.mkdirs();
	}
	
	/**
	 * 用于实现文件下载或者判断文件是否存在
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 文件命令参数
		String cmd = request.getParameter("cmd");
		
		// 类型参数，决定要下载的文件从根目录的哪个子目录中去寻找。
		// 比如type="ECG"，则从rootPath/ECG目录中寻找文件		
		String type = request.getParameter("type"); 
		
		// 文件名参数
		String fileName = request.getParameter("fileName"); 
		if(fileName == null || fileName.equals("")){ 
			throw new ServletException("File Name can't be null or empty"); 
		} 
		 
		File typePath = new File(rootPath, type); 
		File file = new File(typePath, fileName);
		if(!file.exists()){ 
			 throw new IOException("The file doesn't exist at server path:" + file.getAbsolutePath());
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
			int len = 0;
			while((len = fis.read(bufferData))!= -1){
				os.write(bufferData, 0, len);
			}
			os.flush();
			os.close();
			fis.close();
		} else {
			throw new ServletException("cmd wrong.");
		}
	}

	/**
	 * 用于实现文件上传服务
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!ServletFileUpload.isMultipartContent(request)){
			throw new ServletException("Content type is not multipart/form-data");
		}

		DiskFileItemFactory fileFactory = new DiskFileItemFactory();
		fileFactory.setRepository(rootPath);
		ServletFileUpload uploader = new ServletFileUpload(fileFactory);
		
		try {
			List<FileItem> fileItemsList = uploader.parseRequest(request);			
			for(FileItem fileItem : fileItemsList) {
				String type = fileItem.getFieldName().toUpperCase(); // field name 就是类型字符串
				if(Arrays.asList(SUPPORT_FILE_TYPE).contains(type)) {
					String fileName = fileItem.getName();
					File typePath = new File(rootPath, type);
					if(!typePath.exists()) typePath.mkdirs();	
					File file = new File(typePath, fileName);
					fileItem.write(file); // 写信号文件
				}
			}			
		} catch (Exception e) {
			throw new IOException("error occurs when writing data into file.");
		}
	}

}
