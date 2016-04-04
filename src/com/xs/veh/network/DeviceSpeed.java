package com.xs.veh.network;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.common.CharUtil;
import com.xs.veh.entity.Device;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.SpeedData;

import gnu.io.SerialPortEvent;

@Service("deviceSpeed")
@Scope("prototype")
public class DeviceSpeed extends SimpleRead {

	private DeviceSpeedDecode sd;

	private SpeedData speedData;

	private DeviceDisplay display;

	private DeviceSignal signal;
	
	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;
	

	public SpeedData getSpeedData() {
		return speedData;
	}

	public DeviceDisplay getDisplay() {
		return display;
	}

	public DeviceSignal getSignal() {
		return signal;
	}

	public void setSpeedData(SpeedData speedData) {
		this.speedData = speedData;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
	}

	public void setSignal(DeviceSignal signal) {
		this.signal = signal;
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
				
				sd.device2pc(endodedData);
			
			} catch (IOException e) {
				logger.error("读取灯光仪数据流异常", e);
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
		sd = (DeviceSpeedDecode) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		if (dwkg != null) {
			signal = (DeviceSignal) servletContext.getAttribute(dwkg + "_" + Device.KEY);
		}
		sd.init(this);
	}

	public void checkStart() throws IOException, InterruptedException {
		
		sd.startCheck();
		
		// 保存检测数据 //计算检测结果
		this.checkDataManager.saveSpeedData(speedData);
		this.display.sendMessage("速度：" + speedData.getSpeed() / 10.0, DeviceDisplay.XP);
		Thread.sleep(2000);
		this.checkDataManager.saveSpeedData(speedData);
		this.display.sendMessage("检测完毕向前行驶", DeviceDisplay.XP);
		

	}

}
