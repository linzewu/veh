package com.xs.veh.util;

import java.io.IOException;
import java.util.List;
import java.util.TooManyListenersException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.Device;
import com.xs.veh.manager.BaseParamsManager;
import com.xs.veh.manager.DeviceManager;
import com.xs.veh.manager.WorkPointManager;
import com.xs.veh.network.DeviceBrakRoller;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceLight;
import com.xs.veh.network.DeviceSideslip;
import com.xs.veh.network.DeviceSignal;
import com.xs.veh.network.DeviceSpeed;
import com.xs.veh.network.DeviceWeigh;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

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

	private DeviceManager deviceManager;

	private ThreadPoolTaskExecutor executor;
	
	private WorkPointManager workPointManager;
	
	private BaseParamsManager baseParamsManager;

	/**
	 * Default constructor.
	 */
	public InitListener() {
	}
	
	private void init(ServletContext servletContext){
		
		this.servletContext = servletContext;
		wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		baseParamsManager = (BaseParamsManager) wac.getBean("baseParamsManager");
		deviceManager = (DeviceManager) wac.getBean("deviceManager");
		executor = (ThreadPoolTaskExecutor) wac.getBean("taskExecutor");
		workPointManager = (WorkPointManager) wac.getBean("workPointManager");
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent contextEvent) {

		try {
			init(contextEvent.getServletContext());
			
			// 加载参数表
			List<BaseParams> bps = baseParamsManager.getBaseParams();
			servletContext.setAttribute("bps", bps);
			// 打开所有设备
			 openDevice();
			 //启动工位线程
			 workPointManager.startAllWorkPonit();
			 

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}

	private void openDevice() {

		List<Device> devices = deviceManager.getDevicesOfType();

		for (Device device : devices) {

			if (device.getQtxx() == null) {
				log.error("设备信息不完整，无法启动,设备类型==" + device.getType());
			}

			// 光电开关
			if (device.getType() == Device.GDKG) {
				DeviceSignal deviceSignal = (DeviceSignal) wac.getBean("deviceSignal");
				try {
					deviceSignal.setDevice(device);
					if (!deviceSignal.isOpen()) {
						deviceSignal.open();
					}
					if (!deviceSignal.isRun()) {
						this.executor.execute(deviceSignal);
						deviceSignal.setRun(true);
					}
				} catch (InstantiationException | NoSuchPortException | PortInUseException | IOException
						| UnsupportedCommOperationException | TooManyListenersException | IllegalAccessException
						| ClassNotFoundException e) {
					log.error("光电开关打开失败", e);
				}
				servletContext.setAttribute(device.getThredKey(), deviceSignal);
			}

			// 初始化显示屏
			if (device.getType() == Device.XSP) {
				DeviceDisplay dd = (DeviceDisplay) wac.getBean("deviceDisplay");
				
				System.out.println(device.getCom());
				
				try {
					dd.setDevice(device);
					dd.open();
					dd.setDefault();
					
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {

					log.error("显示屏打开失败", e);
				}
				servletContext.setAttribute(device.getThredKey(), dd);
			}

			// 制动设备
			if (device.getType() == Device.ZDJCSB) {
				DeviceBrakRoller dl = (DeviceBrakRoller) wac.getBean("deviceBrakRoller");
				try {
					dl.setDevice(device);
					dl.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {

					log.error("制动设备打开失败", e);
				}
				servletContext.setAttribute(device.getThredKey(), dl);
			}

			// 灯光设备
			if (device.getType() == Device.DGJCSB) {
				DeviceLight dl = (DeviceLight) wac.getBean("deviceLight");
				try {
					dl.setDevice(device);
					dl.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("灯光设备打开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dl);
			}

			// 侧滑设备
			if (device.getType() == Device.CHJCSB) {
				DeviceSideslip dl = (DeviceSideslip) wac.getBean("deviceSideslip");
				try {
					dl.setDevice(device);
					dl.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("侧滑设备打开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dl);
			}

			// 称重
			if (device.getType() == Device.CZJCSB) {
				DeviceWeigh dl = (DeviceWeigh) wac.getBean("deviceWeigh");
				try {
					dl.setDevice(device);
					dl.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("称重设备打开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dl);
			}

			// 速度设备
			if (device.getType() == Device.SDJCSB) {
				DeviceSpeed dl = (DeviceSpeed) wac.getBean("deviceSpeed");
				try {
					dl.setDevice(device);
					dl.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("速度设备打开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dl);
			}
		}

	}

}
