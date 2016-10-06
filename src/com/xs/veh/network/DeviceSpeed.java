package com.xs.veh.network;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.SpeedData;

import gnu.io.SerialPortEvent;

@Service("deviceSpeed")
@Scope("prototype")
public class DeviceSpeed extends SimpleRead implements ICheckDevice {

	private AbstractDeviceSpeed sd;
	
	private DeviceDisplay display;

	private DeviceSignal signal;
	
	private Integer s1;
	
	
	
	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;
	

	public Integer getS1() {
		return s1;
	}

	public DeviceDisplay getDisplay() {
		return display;
	}

	public DeviceSignal getSignal() {
		return signal;
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
					//logger.info("数据长度" + length);
					if (length >= 1024 * 128) {
						logger.debug("读入的数据超过1024 * 128");
						break;
					}
				}
				byte[] endodedData = new byte[length];
				System.arraycopy(readBuffer, 0, endodedData, 0, length);
				
				sd.device2pc(endodedData);
			
			} catch (Exception e) {
				logger.error("速度仪表通讯异常", e);
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
		sd = (AbstractDeviceSpeed) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		
		s1=this.getQtxxObject().getInt("kzsb-xhw");
		
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

	public void startCheck(VehCheckLogin vehCheckLogin,VehFlow vehFlow) throws Exception {
		
		SpeedData speedData = sd.startCheck(vehCheckLogin,vehFlow);
		
		speedData.setBaseDeviceData(vehCheckLogin, checkDataManager.getDxjccs(vehFlow, speedData),
				vehFlow.getJyxm());
		
		//速度限值
		speedData.setSdxz();
		//速度判定
		speedData.setSdpd();
		speedData.setZpd();
		// 保存检测数据 
		this.checkDataManager.saveData(speedData);
		
		this.display.sendMessage("速度：" + CheckDataManager.MathRound(speedData.getSpeed()/1f), DeviceDisplay.XP);
		Thread.sleep(1500);
		
		String jg = speedData.getSdpd()==SpeedData.PDJG_HG?"O":"X";
		
		this.display.sendMessage("判定结果：" + jg, DeviceDisplay.XP);
		
		Thread.sleep(1500);
		
		this.display.sendMessage("检测完毕向前行驶", DeviceDisplay.XP);
		
		boolean flag=true;
		
		while(flag){
			flag = this.signal.getSignal(s1);
			Thread.sleep(200);
		}
		display.setDefault();
	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
