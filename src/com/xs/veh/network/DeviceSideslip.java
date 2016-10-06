package com.xs.veh.network;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.SideslipData;
import com.xs.veh.network.data.SpeedData;

import gnu.io.SerialPortEvent;

/**
 * 
 * @author linze
 *
 */
@Service("deviceSideslip")
@Scope("prototype")
public class DeviceSideslip extends SimpleRead implements ICheckDevice {

	private AbstractDeviceSideslip sd;

	private DeviceDisplay display;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	private DeviceSignal signal;

	public DeviceDisplay getDisplay() {
		return display;
	}

	public CheckDataManager getCheckDataManager() {
		return checkDataManager;
	}

	public DeviceSignal getSignal() {
		return signal;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
	}

	public void setCheckDataManager(CheckDataManager checkDataManager) {
		this.checkDataManager = checkDataManager;
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
					//logger.info("数据长度" + length);
					if (length >= 1024 * 128) {
						logger.debug("读入的数据超过1024 * 128");
						break;
					}
				}
				byte[] endodedData = new byte[length];
				System.arraycopy(readBuffer, 0, endodedData, 0, length);
				// logger.info("有侧滑数据返回:"+CharUtil.byte2HexOfString(endodedData));
				sd.device2pc(endodedData);

			} catch (Exception e) {
				logger.error("侧滑数据异常", e);
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
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		String dwkg = (String) this.getQtxxObject().get("kzsb-dwkg");
		if (dwkg != null) {
			signal = (DeviceSignal) servletContext.getAttribute(dwkg + "_" + Device.KEY);
		}
		sd = (AbstractDeviceSideslip) Class.forName(this.getDevice().getDeviceDecode()).newInstance();

		sd.init(this);

	}

	/**
	 * 侧滑
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SystemException
	 */
	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow) throws Exception {
		SideslipData sideslipData = sd.startCheck(vehFlow);
		sideslipData.setBaseDeviceData(vehCheckLogin, 1, vehFlow.getJyxm());

		sideslipData.setBaseDeviceData(vehCheckLogin, checkDataManager.getDxjccs(vehFlow, sideslipData),
				vehFlow.getJyxm());

		// 侧滑限制
		sideslipData.setChxz();
		// 侧滑判定
		sideslipData.setChpd(vehCheckLogin);
		sideslipData.setZpd();
		this.checkDataManager.saveData(sideslipData);
		Thread.sleep(2000);
		
		String jg="-";
		
		if(sideslipData.getChpd() == SideslipData.PDJG_HG){
			jg="O";
		}else if(sideslipData.getChpd() == SideslipData.PDJG_BHG){
			jg="X";
		}
		
		
		this.display.sendMessage("判定结果：" + jg, DeviceDisplay.XP);
		Thread.sleep(1500);
		display.setDefault();
	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
