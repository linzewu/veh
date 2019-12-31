package com.xs.veh.network;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.CurbWeightData;
import com.xs.veh.network.driver.DeviceManyWeighDriverOfJXZB10;

import gnu.io.SerialPortEvent;

/**
 * 
 * @author linze
 *
 */
@Service("deviceManyWeigh")
@Scope("prototype")
public class DeviceManyWeigh extends SimpleRead  {
	
	static Logger logger = Logger.getLogger(DeviceManyWeigh.class);

	private AbstractDeviceManyWeigh dw;

	private DeviceDisplay display;

	private DeviceSignal signal;
	

	private Integer s1;
	
	private Integer s2;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private CheckDataManager checkDataManager;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	private VehCheckLogin vehCheckLogin;
	
	

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}

	public Integer getS1() {
		return s1;
	}

	public void setS1(Integer s1) {
		this.s1 = s1;
	}

	public DeviceDisplay getDisplay() {
		return display;
	}

	public DeviceSignal getSignal() {
		return signal;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
	}

	public void setSignal(DeviceSignal signal) {
		this.signal = signal;
	}


	public Integer getS2() {
		return s2;
	}

	public void setS2(Integer s2) {
		this.s2 = s2;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:// 当有可用数据时读取数据,并且给串口返回数据

			byte[] readBuffer = new byte[1024 * 128];
			int length = 0;
			int lengthTemp = 0;
			try {
				while (inputStream.available() > 0) {
					lengthTemp = inputStream.read(readBuffer);
					length += lengthTemp;
					if (length >= 1024 * 128) {
						logger.error("读入的数据超过1024 * 128");
						break;
					}
				}
				byte[] endodedData = new byte[length];
				// logger.info("数据长度："+endodedData.length);
				System.arraycopy(readBuffer, 0, endodedData, 0, length);
				dw.device2pc(endodedData);
			} catch (Exception e) {
				logger.error("称重台通讯异常", e);
			}
			break;
		}

	}

	@Override
	public void run() {

	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String temp = (String) this.getQtxxObject().get("kzsb-xsp");
		String dwkg = (String) this.getQtxxObject().get("kzsb-dwkg");
		//s1 = getQtxxObject().getInt("kzsb-xhw");

		dw = (AbstractDeviceManyWeigh) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		if (dwkg != null) {
			signal = (DeviceSignal) servletContext.getAttribute(dwkg + "_" + Device.KEY);
		}
		dw.init(this);
	}

	/**
	 * 称重
	 * 
	 * @param vehCheckLogin
	 * @param vehFlow
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public CurbWeightData startCheck(VehCheckLogin vehCheckLogin) throws Exception, InterruptedException {
		
		logger.info("整备质量检测开始");
		
		this.vehCheckLogin=vehCheckLogin;
		CurbWeightData curbWeightData = dw.startCheck(vehCheckLogin);
		Thread.sleep(2000);
		this.display.sendMessage("检测完毕向前行驶", DeviceDisplay.XP);
		Thread.sleep(2000);
		display.setDefault();
		
		return curbWeightData;
	}
	
	public void setDw(Integer zw) {
		
		if(zw==0) {
			dw.qzdw=true;
		}
		
		if(zw==1) {
			dw.hzdw=true;
		}
		
	}

}
