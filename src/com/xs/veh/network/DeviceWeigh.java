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

import gnu.io.SerialPortEvent;

/**
 * 
 * @author linze
 *
 */
@Service("deviceWeigh")
@Scope("prototype")
public class DeviceWeigh extends SimpleRead {

	private IDeviceWeighDecode dw;

	private WeighData weighData;

	private DeviceDisplay display;
	
	private DeviceSignal signal; 
	
	private Integer s1;
	
	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;
	
	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executor;

	
	//开始称重
	private String kscz;
	
	private String jscz;

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
				
				ProtocolType type = dw.getProtocolType(endodedData);
				// 响应数据的处理方法
				if (type == ProtocolType.DATA) {
					dw.setData(endodedData, weighData);
				}


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
		dw = (IDeviceWeighDecode) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		s1=this.getQtxxObject().getInt("kzsb-xhw");
		
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
			
		}
		
		if(dwkg!=null){
			signal=(DeviceSignal)servletContext.getAttribute(dwkg + "_" + Device.KEY);
		}
		
		kscz =(String) this.getQtxxObject().get("t-kscz");
		jscz =(String) this.getQtxxObject().get("t-jscz");
	}

	/**
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void checkStart() throws IOException, InterruptedException {


		// 开始新的一次检测
		createNew();
		// 显示屏显示信息
		this.display.sendMessage("苏J00001", DeviceDisplay.SP);
		this.display.sendMessage("前轴称重请到位", DeviceDisplay.XP);
		
		//开始称重
		this.sendMessage(kscz);
		int i=0;
		while(true){
			
			if(this.signal.getSignal(s1)){
				if(weighData.getLeftData()!=null&&weighData.getRightData()!=null){
					this.display.sendMessage("前轴称重已到位", DeviceDisplay.SP);
					this.display.sendMessage((weighData.getLeftData()+weighData.getRightData())+"KG", DeviceDisplay.XP);
				}
				i++;
			}else{
				this.display.sendMessage("苏J00001", DeviceDisplay.SP);
				this.display.sendMessage("前轴称重请到位", DeviceDisplay.XP);
				i=0;
			}
			
			if(i>=6){
				break;
			}
			
			Thread.sleep(500);
		}
		
		this.sendHead(jscz);
		
		this.display.sendMessage("前轴称重结束", DeviceDisplay.SP);
		this.display.sendMessage((weighData.getLeftData()+weighData.getRightData())+"KG", DeviceDisplay.XP);
		
	}

	private void createNew() {
		this.weighData = new WeighData();

	}

}
