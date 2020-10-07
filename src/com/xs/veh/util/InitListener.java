package com.xs.veh.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.aspose.words.License;
import com.sun.jna.Native;
import com.xs.common.InitServerCommonUtil;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.PowerPoint;
import com.xs.veh.manager.BaseParamsManager;
import com.xs.veh.manager.DeviceManager;
import com.xs.veh.manager.WorkPointManager;
import com.xs.veh.network.DeviceBrakRoller;
import com.xs.veh.network.DeviceBrakePad;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceDyno;
import com.xs.veh.network.DeviceLight;
import com.xs.veh.network.DeviceManyWeigh;
import com.xs.veh.network.DeviceSideslip;
import com.xs.veh.network.DeviceSignal;
import com.xs.veh.network.DeviceSpeed;
import com.xs.veh.network.DeviceSuspension;
import com.xs.veh.network.DeviceVolume;
import com.xs.veh.network.DeviceWeigh;
import com.xs.veh.network.SimpleRead;
import com.xs.veh.network.driver.LED192;

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
	
	private InitServerCommonUtil initServerCommonUtil;

	/**
	 * Default constructor.
	 */
	public InitListener() {
	}

	private void init(ServletContext servletContext) throws Exception {

		this.servletContext = servletContext;
		wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		baseParamsManager = (BaseParamsManager) wac.getBean("baseParamsManager");
		deviceManager = (DeviceManager) wac.getBean("deviceManager");
		executor = (ThreadPoolTaskExecutor) wac.getBean("taskExecutor");
		workPointManager = (WorkPointManager) wac.getBean("workPointManager");
		initServerCommonUtil = (InitServerCommonUtil) wac.getBean("initServerCommonUtil");
		
		InputStream license = InitListener.class.getClassLoader().getResourceAsStream("license.xml");
    	License aposeLic = new License();
        aposeLic.setLicense(license);
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent contextEvent) {

		try {
			
			init(contextEvent.getServletContext());
			
			List<PowerPoint> powerPoints = initServerCommonUtil.initPower(new String[] {"com.xs.veh.controller"});
			
			servletContext.setAttribute("powerPoints", powerPoints);
		
			
			// 加载参数表
			List<BaseParams> bps = baseParamsManager.getBaseParams();
			servletContext.setAttribute("bps", bps);
			servletContext.setAttribute("bpsMap", bpslist2map(bps));
			// 打开所有设备
			openDevice();
			// 启动工位线程
			workPointManager.setServletContext(servletContext);
			workPointManager.startAllWorkPonit();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	private Map<String, List<BaseParams>> bpslist2map(List<BaseParams> bps ){
		
		Map<String, List<BaseParams>> map =new HashMap<String, List<BaseParams>>();
		
		for(BaseParams bp: bps) {
			
			List<BaseParams> itemList = map.get(bp.getType());
			if(itemList==null) {
				itemList=new ArrayList<BaseParams>();
				map.put(bp.getType(), itemList);
			}
			itemList.add( bp);
		}
		
		return map;
	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}

	private void openDevice() {

		List<Device> devices = deviceManager.getDevicesOfType();

		for (Device device : devices) {

			if (device.getQtxx() == null) {
				log.error("设备信息不完整，无法启动,设备类型==" + device.getType());
				return;
			}

			// 光电开关
			else if (device.getType() == Device.GDKG) {
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
			else if (device.getType() == Device.XSP) {
				DeviceDisplay dd = (DeviceDisplay) wac.getBean("deviceDisplay");
				try {
					dd.setDevice(device);
					
					List<BaseParams> ddls = baseParamsManager.getBaseParamByType("xspddl");
					
					if(ddls.isEmpty()) {
						dd.open();
						dd.setDefault();
					}else {
						
					}
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {

					log.error("显示屏打开失败", e);
				}
				servletContext.setAttribute(device.getThredKey(), dd);
			}

			// 制动设备
			else if (device.getType() == Device.ZDJCSB) {
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
			else if (device.getType() == Device.DGJCSB) {
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
			else if (device.getType() == Device.CHJCSB) {
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
			else if (device.getType() == Device.CZJCSB) {
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

			// 平板设备
			else if (device.getType() == Device.ZDPBSB) {
				DeviceBrakePad dbp = (DeviceBrakePad) wac.getBean("deviceBrakePad");
				try {
					dbp.setDevice(device);
					dbp.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("平板设备打开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dbp);
			}

			// 速度设备
			else if (device.getType() == Device.SDJCSB) {
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
			
			else if(device.getType() ==Device.XJSB) {
				DeviceSuspension dl = (DeviceSuspension) wac.getBean("deviceSuspension");
				try {
					dl.setDevice(device);
					dl.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("悬架设备打开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dl);
			}
			
			else if(device.getType() ==Device.CGJ) {
				DeviceDyno dl = (DeviceDyno) wac.getBean("deviceDyno");
				try {
					dl.setDevice(device);
					dl.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("测功机打开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dl);
			}
			
			else if(device.getType() ==Device.SJJ) {
				DeviceVolume dv = (DeviceVolume) wac.getBean("deviceVolume");
				try {
					dv.setDevice(device);
					dv.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("声级计开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dv);
			}
			else if(device.getType() == Device.DZCZT) {
				DeviceManyWeigh dmw = (DeviceManyWeigh) wac.getBean("deviceManyWeigh");
				try {
					dmw.setDevice(device);
					dmw.open();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
						| PortInUseException | IOException | UnsupportedCommOperationException
						| TooManyListenersException e) {
					log.error("多轴称重台打开异常", e);
				}
				servletContext.setAttribute(device.getThredKey(), dmw);
			}
			else {
				String springName =device.getDeviceSpringName();
				if(springName!=null) {
					
					SimpleRead sr = (SimpleRead) wac.getBean(springName);
					try {
						sr.setDevice(device);
						sr.open();
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchPortException
							| PortInUseException | IOException | UnsupportedCommOperationException
							| TooManyListenersException e) {
						log.error(device.getName()+"设备打开异常", e);
					}
					servletContext.setAttribute(device.getThredKey(), sr);
					
				}else {
					log.error("设备SpringName为null，请检查代码");
				}
				
			}
		}

	}

}
