package com.cmtech.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.cmtech.web.util.MySQLUtil;

public class MyServletContextListener implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		//ServletContextListener.super.contextInitialized(sce);
		ServletContext sc = sce.getServletContext();
		String DBURL = sc.getInitParameter("DBURL");
		String DBUSER = sc.getInitParameter("DBUSER");
		String DBPASSWORD = sc.getInitParameter("DBPASSWORD");
		MySQLUtil.connect(DBURL, DBUSER, DBPASSWORD);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		//ServletContextListener.super.contextDestroyed(sce);
		MySQLUtil.disconnect();
	}

}
