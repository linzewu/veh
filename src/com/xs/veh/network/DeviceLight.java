package com.xs.veh.network;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.xs.common.CharUtil;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.manager.CheckDataManager;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("deviceLight")
@Scope("prototype")
public class DeviceLight extends SimpleRead {


	@Autowired
	private ServletContext servletContext;

	private DeviceDisplay display;
	

	public DeviceDisplay getDisplay() {
		return display;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
	}


	// 灯光仪解码器
	private DeviceLightDecode dld;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executor;

	private DeviceSignal deviceSignal1;

	private DeviceSignal deviceSignal2;

	private Integer s1;

	private Integer s2;
	

	
	

	public DeviceLight() {
	}

	public DeviceLight(Device device) throws NoSuchPortException, TooManyListenersException, PortInUseException,
			UnsupportedCommOperationException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(device);
		init();

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
					logger.info("数据长度" + length);
					if (length >= 1024 * 128) {
						logger.debug("读入的数据超过1024 * 128");
						break;
					}
				}
				byte[] endodedData = new byte[length];
				System.arraycopy(readBuffer, 0, endodedData, 0, length);
				dld.device2pc(endodedData);
			} catch (IOException e) {
				logger.error("读取灯光仪数据流异常", e);
			}
			break;
		}
	}


	@Override
	public void run() {

	}

	public void sysSetting() throws IOException, InterruptedException {
		this.dld.sysSetting();
	}


	/**
	 * 灯光开始测试
	 * 
	 * @param setting
	 *            测试参数
	 * @param clzd
	 *            测量左灯 命令 ，不测则给null
	 * @param clyd
	 *            测量右灯 命令 ，不测则给null
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SystemException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public void startCheck(Map<String, String> setting, String clzd, String clyd)
			throws IOException, InterruptedException  {

		// 等待到位
		int i = 0;
		getDisplay().sendMessage("苏J00001", DeviceDisplay.SP);
		getDisplay().sendMessage("请至停止线", DeviceDisplay.XP);
		while (true) {
			if (deviceSignal1.getSignal(s1) && !deviceSignal2.getSignal(s2)) {
				i++;
				if (i == 1) {
					this.display.sendMessage("苏J00001", DeviceDisplay.SP);
					this.display.sendMessage("停止", DeviceDisplay.XP);
				}
			}

			if (!deviceSignal1.getSignal(s1)) {
				this.display.sendMessage("苏J00001", DeviceDisplay.SP);
				this.display.sendMessage("请至停止线", DeviceDisplay.XP);
				i = 0;
			} else if (deviceSignal2.getSignal(s2)) {
				this.display.sendMessage("苏J00001", DeviceDisplay.SP);
				this.display.sendMessage("退后", DeviceDisplay.XP);
				i = 0;
			}
			if (i >= 6) {
				break;
			}
			Thread.sleep(500);
		}
		
		dld.clear();
		dld.startCheck(clzd, clyd);
	}

	/**
	 * 创建测试数据
	 * 
	 * @return
	 */
	public Map<String, String> createSettingData() {
		return dld.createSettingData();
	}

	
	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// 初始化灯光仪解码器
		dld = (DeviceLightDecode) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		JSONObject qtxx = this.getQtxxObject();
		String temp = (String) qtxx.get("kzsb-xsp");
		String dwkg1 = (String) qtxx.get("kzsb-dwkg1");
		String dwkg2 = (String) qtxx.get("kzsb-dwkg2");
		s1 = Integer.valueOf(qtxx.getString("kzsb-xhw1"));
		s2 = Integer.valueOf(qtxx.getString("kzsb-xhw2"));

		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		// 型号开关
		if (dwkg1 != null) {
			deviceSignal1 = (DeviceSignal) servletContext.getAttribute(dwkg1 + "_" + Device.KEY);
		}
		if (dwkg2 != null) {
			deviceSignal2 = (DeviceSignal) servletContext.getAttribute(dwkg2 + "_" + Device.KEY);
		}
		
		dld.setDeviceLight(this);
	}
	

}
