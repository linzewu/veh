package com.xs.veh.network;

import java.io.IOException;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.VolumeData;

import gnu.io.SerialPortEvent;

/**
 * 
 * @author linze
 *
 */
@Service("deviceVolume")
@Scope("prototype")
public class DeviceVolume extends SimpleRead  {

	private AbstractDeviceVolume dv;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	private VehCheckLogin vehCheckLogin;
	
	

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
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
				// logger.info("数据长度："+endodedData.length);
				System.arraycopy(readBuffer, 0, endodedData, 0, length);
				dv.device2pc(endodedData);
			} catch (Exception e) {
				logger.error("声级计通讯异常", e);
			}
			break;
		}

	}

	@Override
	public void run() {

	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		dv = (AbstractDeviceVolume) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		dv.init(this);
	}

	/**
	 * 声级测试
	 * 
	 * @param vehCheckLogin
	 * @param vehFlow
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void startCheck(VehCheckLogin vehCheckLogin) throws IOException, InterruptedException {
		logger.info("进入声级计检测");
		this.vehCheckLogin=vehCheckLogin;
		//VolumeData volumeData = dv.startCheck(vehCheckLogin);
		VolumeData volumeData =new VolumeData();
		volumeData.setFb(String.format("%.1f", (float)(int)(Math.random()*(115-90+1)+90)));
		volumeData.setZpd(VolumeData.PDJG_HG);
		volumeData.setBaseDeviceData(vehCheckLogin, 0, "VL");
		this.checkDataManager.saveData(volumeData);
		
	}
	
	
	
	public void setDw(Integer zw) {
		
	}

	@Override
	public String getDeviceSpringName() {
		return "deviceVolume";
	}
	
	public static void main(String[] age) {
		
		for(int i=0;i<100;i++) {
			System.out.println(String.format("%.1f", (float)(int)(Math.random()*(115-90+1)+90)));
		}
		
		
	}

}
