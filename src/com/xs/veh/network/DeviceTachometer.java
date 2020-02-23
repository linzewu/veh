package com.xs.veh.network;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.DynoData;

import gnu.io.SerialPortEvent;
@Service("deviceTachometer")
@Scope("prototype")
/**
 * 转速度计
 * @author linze
 *
 */
public class DeviceTachometer extends SimpleRead implements ICheckDevice {
	
	private AbstractDeviceTachometer adt;
	private VehCheckLogin vehCheckLogin;
	
	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;
	
	@Autowired
	private ServletContext servletContext;



	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}
	
	

	public AbstractDeviceTachometer getDs() {
		return adt;
	}

	public void setDs(AbstractDeviceTachometer adt) {
		this.adt = adt;
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
				adt.device2pc(endodedData);
			} catch (Exception e) {
				logger.error("测功机通讯异常", e);
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

	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows, Map<String, Object> otherParam)
			throws InterruptedException, IOException, SystemException {

	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		adt = (AbstractDeviceTachometer) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		adt.init(this);
	}

	@Override
	public String getDeviceSpringName() {
		return "deviceTachometer";
	}

	
	

}
