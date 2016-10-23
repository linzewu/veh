package com.xs.veh.network;

import java.io.IOException;
import java.util.List;
import java.util.TooManyListenersException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.BrakRollerData;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;

@Service("deviceBrakePad")
@Scope("prototype")
public class DeviceBrakePad extends SimpleRead implements ICheckDevice {

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	private static Logger logger = Logger.getLogger(DeviceBrakePad.class);

	private AbstractDeviceBrakePad dbp;

	private DeviceDisplay display;

	@Autowired
	private ServletContext servletContext;

	public DeviceDisplay getDisplay() {
		return display;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
	}

	public DeviceBrakePad(Device device)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchPortException,
			TooManyListenersException, PortInUseException, UnsupportedCommOperationException, IOException {
		super(device);
		init();
	}
	
	public DeviceBrakePad(){
		
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
			// 制动返回数据
			byte[] readBuffer = new byte[1024 * 64];
			int length = 0;
			int lengthTemp = 0;
			try {
				while (inputStream.available() > 0) {
					lengthTemp = inputStream.read(readBuffer);
					length += lengthTemp;
					// logger.info("数据长度" + length);
					if (length >= 1024 * 64) {
						logger.debug("读入的数据超过1024 * 64");
						break;
					}
				}
				byte[] endodedData = new byte[length];
				System.arraycopy(readBuffer, 0, endodedData, 0, length);

				dbp.device2pc(endodedData);

			} catch (Exception e) {
				logger.error("制动设备获取数据异常", e);
			}
			break;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows) throws Exception {
		
		List<BrakRollerData> datas = dbp.startCheck(vehFlows);
		
		boolean sfhg=true;

		for (BrakRollerData brakRollerData : datas) {
			
			// 设置基础数据
			brakRollerData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), brakRollerData.getJyxm());
			// 非驻车制动则计算检测结果
			if (!brakRollerData.getJyxm().equals("B0")) {
				// 空载行车制动率
				brakRollerData.setKzxczdl(vehCheckLogin);
				// 空载制动率限制及判定
				brakRollerData.setKzzdlxz(vehCheckLogin);
				brakRollerData.setKzzdlpd();

				// 设置空载不平衡率
				brakRollerData.setKzbphl();
				// 设置不平衡率限值
				brakRollerData.setBphlxz(vehCheckLogin);
				// 空载不平衡率判定
				brakRollerData.setKzbphlpd();

				brakRollerData.setJzzdl();
				// 加载制动率限制及判定
				brakRollerData.setJzzdlxz(vehCheckLogin);
				brakRollerData.setJzzdlpd();
				// 加载不平衡率判定
				brakRollerData.setJzbphlpd();
			}
			brakRollerData.setZpd();
			this.checkDataManager.saveData(brakRollerData);
			
			brakRollerData.getZpd();
			
			if (brakRollerData.getZpd() == BrakRollerData.PDJG_BHG){
				sfhg=false;
			}
		}
		if(sfhg){
			display.sendMessage("检判定结果：O", DeviceDisplay.XP);
		}else{
			display.sendMessage("检判定结果：X", DeviceDisplay.XP);
		}
		
		Thread.sleep(1500);
		display.sendMessage("请向前行驶", DeviceDisplay.XP);
		
		Thread.sleep(2000);
		this.display.setDefault();

	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// 初始化制动设备
		dbp = (AbstractDeviceBrakePad) Class.forName(this.getDevice().getDeviceDecode()).newInstance();

		
		String temp = (String) this.getQtxxObject().get("kzsb-xsp");
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		
		dbp.init(this);
		
	}

}
