package com.xs.veh.network;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.network.data.SideslipData;

import gnu.io.SerialPortEvent;

/**
 * 
 * @author linze
 *
 */
@Service("deviceSideslip")
@Scope("prototype")
public class DeviceSideslip extends SimpleRead implements ICheckDevice {
	
	private Logger logger = Logger.getLogger(DeviceSideslip.class);

	private AbstractDeviceSideslip sd;

	private DeviceDisplay display;
	
	private VehCheckLogin vehCheckLogin;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;
	
	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;

	private DeviceSignal signal;
	
	

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}

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
		
		logger.info("侧滑解码器："+this.getDevice().getDeviceDecode());
		
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
	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow,Map<String,Object> otherParam) throws InterruptedException, IOException{
		
		VehCheckProcess process = this.checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(),
				vehFlow.getJyxm());
		//process.setKssj(new Date());
		
		SideslipData sideslipData =check(vehCheckLogin,vehFlow,otherParam,1);
		
		SideslipData hSideslipData=null;
		
		Thread.sleep(200);
		if(vehCheckLogin.getCheckType()==1&&vehCheckLogin.getZxzs()==2) {
			hSideslipData=check(vehCheckLogin,vehFlow,otherParam,2);
		}
		
		
		
		String jg="-";
		if(sideslipData.getChpd() == SideslipData.PDJG_HG){
			jg="O";
		}else if(sideslipData.getChpd() == SideslipData.PDJG_BHG){
			jg="X";
		}
		this.display.sendMessage("前转向结果：" + jg, DeviceDisplay.XP);
		Thread.sleep(2000);
		
		if(hSideslipData!=null) {
			if(hSideslipData.getChpd() == SideslipData.PDJG_HG){
				jg="O";
			}else if(hSideslipData.getChpd() == SideslipData.PDJG_BHG){
				jg="X";
			}
			this.display.sendMessage("后转向结果：" + jg, DeviceDisplay.XP);
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND)+10);
		process.setJssj(calendar.getTime());
		
		calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND)-20);
		
		process.setKssj(calendar.getTime());
		
		process.setJcxdh(this.getDevice().getJcxxh());
		this.checkDataManager.updateProcess(process);
		
		Thread.sleep(2000);
		display.setDefault();
		
		VehCheckProcess vp=process;
		
		checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C55", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
				vp.getClsbdh(),vehCheckLogin.getVehcsbj());
		Thread.sleep(100);
		checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C81", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
				vp.getClsbdh(),vehCheckLogin.getVehcsbj());
		Thread.sleep(100);
		checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C58", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
				vp.getClsbdh(),vehCheckLogin.getVehcsbj());
		
	}
	
	
	
	private SideslipData check(VehCheckLogin vehCheckLogin, VehFlow vehFlow,Map<String,Object> otherParam,Integer zs) throws InterruptedException, IOException{
		this.vehCheckLogin=vehCheckLogin;
		
		
		SideslipData sideslipData = sd.startCheck(vehFlow,zs);
		
		sideslipData.setBaseDeviceData(vehCheckLogin, 1, vehFlow.getJyxm());

		sideslipData.setBaseDeviceData(vehCheckLogin, checkDataManager.getDxjccs(vehFlow, sideslipData),
				vehFlow.getJyxm());

		// 侧滑限制
		sideslipData.setChxz();
		// 侧滑判定
		sideslipData.setChpd(vehCheckLogin);
		sideslipData.setZpd();
		sideslipData.setStrData();
		sideslipData.setZxzs(zs);
		this.checkDataManager.saveData(sideslipData);
		return sideslipData;
	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows,Map<String,Object> otherParam){
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDeviceSpringName() {
		return "deviceSideslip";
	}

}
