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

import gnu.io.SerialPortEvent;

@Service("deviceSpeed")
@Scope("prototype")
public class DeviceSpeed extends SimpleRead {

	private IDeviceSpeedDecode sd;

	private SpeedData speedData;

	private DeviceDisplay display;

	private boolean checkingFlag;

	private DeviceSignal signal;

	private Integer s1;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	private String up;

	private String down;

	private String end;

	private String qs;

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
				ProtocolType type = sd.getProtocolType(endodedData);
				// 响应数据的处理方法
				if (type == ProtocolType.DATA) {
					sd.setData(endodedData, speedData);
					// 结束过程
					this.checkingFlag = false;
				}

				// 响应通知的方法
				if (type == ProtocolType.NOTICE) {
					String ml = CharUtil.byte2HexOfString(endodedData);
					if (ml.equals(end)) {
						this.sendMessage(qs);
					}
				}

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
		s1 = this.getQtxxObject().getInt("kzsb-xhw");
		
		sd = (IDeviceSpeedDecode) Class.forName(this.getDevice().getDeviceDecode()).newInstance();

		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}

		if (dwkg != null) {
			signal = (DeviceSignal) servletContext.getAttribute(dwkg + "_" + Device.KEY);
		}

		up = (String) this.getQtxxObject().get("t-up");
		down = (String) this.getQtxxObject().get("t-down");
		end = (String) this.getQtxxObject().get("r-end");
		qs = (String) this.getQtxxObject().get("g-qs");

	}

	public void checkStart() throws IOException, InterruptedException {

		// 开始新的一次检测
		createNew();
		// 显示屏显示信息
		this.display.sendMessage("苏J00001", DeviceDisplay.SP);
		this.display.sendMessage("速度上线检测", DeviceDisplay.XP);
		
		int i=0;
		while(true){
			if(this.signal.getSignal(s1)){
				this.display.sendMessage("苏J00001", DeviceDisplay.SP);
				this.display.sendMessage("速度到位", DeviceDisplay.XP);
				i++;
			}else{
				this.display.sendMessage("苏J00001", DeviceDisplay.SP);
				this.display.sendMessage("速度上线检测", DeviceDisplay.XP);
				i=0;
			}
			
			if(i>=6){
				break;
			}
			
			Thread.sleep(500);
		}

		// 速度太下降
		this.sendMessage(down);

		// 等待测量结束
		while (this.checkingFlag) {
			Thread.sleep(500);
		}

		// 保存检测数据 //计算检测结果
		this.checkDataManager.saveSpeedData(speedData);
		this.display.sendMessage("速度：O", DeviceDisplay.XP);
		Thread.sleep(2000);

	}

	private void createNew() {
		this.checkingFlag = true;
		this.speedData = new SpeedData();
	}

}
