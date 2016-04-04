package com.xs.veh.network;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.Device;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.WeighData;

import gnu.io.SerialPortEvent;

/**
 * 
 * @author linze
 *
 */
@Service("deviceWeigh")
@Scope("prototype")
public class DeviceWeigh extends SimpleRead {

	private DeviceWeighDecode dw;

	private WeighData weighData;

	private DeviceDisplay display;

	private DeviceSignal signal;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executor;

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
				System.arraycopy(readBuffer, 0, endodedData, 0, length);
				dw.device2pc(endodedData);
			} catch (IOException e) {
				logger.error("称重台数据流异常", e);
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
		dw = (DeviceWeighDecode) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
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
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void checkStart() throws IOException, InterruptedException {

	}

}
