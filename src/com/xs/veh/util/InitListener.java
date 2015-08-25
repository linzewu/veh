package com.xs.veh.util;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.xs.veh.entity.BaseParams;
import com.xs.veh.manager.BaseParamsManager;

/**
 * Application Lifecycle Listener implementation class InitListener
 * 
 */
public class InitListener implements ServletContextListener {

	protected static Log log = LogFactory.getLog(InitListener.class);

	private SessionFactory sessionFactory;

	private SessionFactory trafficeSessionFactory;

	private WebApplicationContext wac;

	private ServletContext servletContext;


	/**
	 * Default constructor.
	 */
	public InitListener() {
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent contextEvent) {

		try {
			servletContext = contextEvent.getServletContext();

			wac = WebApplicationContextUtils
					.getWebApplicationContext(contextEvent.getServletContext());
			BaseParamsManager baseParamsManager = (BaseParamsManager) wac.getBean("baseParamsManager");
			
			//加载参数表
			List<BaseParams> bps= baseParamsManager.getBaseParams();
			servletContext.setAttribute("bps", bps);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void contextDestroyed(ServletContextEvent arg0) {
	}


}
