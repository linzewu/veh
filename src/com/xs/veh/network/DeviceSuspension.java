package com.xs.veh.network;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.SuspensionData;

import gnu.io.SerialPortEvent;
@Service("deviceSuspension")
@Scope("prototype")
public class DeviceSuspension extends SimpleRead implements ICheckDevice {
	
	private AbstractDeviceSuspension ds;
	private DeviceDisplay display;
	private VehCheckLogin vehCheckLogin;
	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	public DeviceDisplay getDisplay() {
		return display;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
	}

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}

	@Override
	public void serialEvent(SerialPortEvent ev) {
		switch (ev.getEventType()) {
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
				ds.device2pc(endodedData);
			} catch (Exception e) {
				logger.error("悬架仪通讯异常", e);
			}
			break;
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow, Map<String, Object> otherParam)
			throws IOException, InterruptedException, SystemException {
		this.vehCheckLogin=vehCheckLogin;
		
		VehCheckProcess process = this.checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(),
				vehFlow.getJyxm());
		process.setKssj(new Date());
		
		SuspensionData suspensionData = ds.startCheck(vehCheckLogin, vehFlow);
		
		process.setJssj(new Date());
		this.checkDataManager.updateProcess(process);
		
		//sideslipData.setBaseDeviceData(vehCheckLogin, 1, vehFlow.getJyxm());

		suspensionData.setBaseDeviceData(vehCheckLogin, checkDataManager.getDxjccs(vehFlow, suspensionData),
				vehFlow.getJyxm());

		Thread.sleep(2000);
		this.display.sendMessage("检测完毕向前行驶", DeviceDisplay.XP);
//		boolean flag = true;
//
//		while (flag) {
//			flag = this.signal.getSignal(s1);
//			Thread.sleep(200);
//		}
		this.checkDataManager.saveData(suspensionData);
		display.setDefault();
	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows, Map<String, Object> otherParam)
			throws InterruptedException, IOException, SystemException {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		// 初始悬架仪解码器
		ds = (AbstractDeviceSuspension) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		ds.init(this);

	}

}
