package com.xs.veh.network;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TooManyListenersException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.network.data.LightData;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;
import net.sf.json.JSONObject;

@Service("deviceLight")
@Scope("prototype")
public class DeviceLight extends SimpleRead implements ICheckDevice {

	@Autowired
	private ServletContext servletContext;

	private DeviceDisplay display;
	
	private String kw="L";

	public DeviceDisplay getDisplay() {
		return display;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
	}

	// 灯光仪解码器
	private AbstractDeviceLight dld;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executor;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;
	
	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;
	
	

	public String getKw() {
		return kw;
	}

	public void setKw(String kw) {
		this.kw = kw;
	}

	public DeviceLight() {
	}

	public DeviceLight(Device device) throws NoSuchPortException, TooManyListenersException, PortInUseException,
			UnsupportedCommOperationException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(device);
		init();

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
				dld.device2pc(endodedData);
			} catch (Exception e) {
				logger.error("读取灯光仪数据流异常", e);
			}
			break;
		}
	}

	@Override
	public void run() {

	}

	public void sysSetting() throws Exception {
		this.dld.sysSetting();
	}

	/**
	 * 
	 * @param vehCheckLogin
	 * @param vheFlows
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vheFlows) throws Exception {

		dld.createNewList();
		
		List<LightData> datas = dld.startCheck(vehCheckLogin, vheFlows);
		
		
		String jg = (datas == null || datas.size() == 0) ? "X" : "O";
		for (LightData data : datas) {
			data.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), data.getJyxm());
			data.setCzpy();
			// 设置灯光光强的判定限制
			data.setDgpdxz(vehCheckLogin);
			// 设置光强判定
			data.setGqpd();

			// 设置垂直偏移限值
			data.setCzpyxz(vehCheckLogin);
			data.setCzpypd();
			data.setZpd();
			this.checkDataManager.saveData(data);
			if (data.getZpd() == CheckDataManager.PDJG_BHG) {
				jg = "X";
			}
		}
		display.sendMessage(vehCheckLogin.getHphm() + "检测完成", DeviceDisplay.SP);
		display.sendMessage("判定结果：" + jg, DeviceDisplay.XP);

		// 灯光检测完成后等待8秒 仪器归为时间
		Thread.sleep(3000);
		display.sendMessage(vehCheckLogin.getHphm() + "检测完成", DeviceDisplay.SP);
		display.sendMessage("请等待！", DeviceDisplay.XP);
		Thread.sleep(3000);
		display.sendMessage(vehCheckLogin.getHphm() + "检测完成", DeviceDisplay.SP);
		display.sendMessage("请向前行驶", DeviceDisplay.XP);
		// 判定车是否离开 如果没有离开，则等待是否复位 ，如果离开则结束检测
		while (dld.deviceSignal1.getSignal(dld.s1) && !dld.deviceSignal2.getSignal(dld.s2)) {
			Thread.sleep(300);
		}
		display.setDefault();
	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// 初始化灯光仪解码器
		dld = (AbstractDeviceLight) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		JSONObject qtxx = this.getQtxxObject();
		String temp = (String) qtxx.get("kzsb-xsp");
		String dwkg1 = (String) qtxx.get("kzsb-dwkg1");
		String dwkg2 = (String) qtxx.get("kzsb-dwkg2");

		dld.s1 = Integer.valueOf(qtxx.getString("kzsb-xhw1"));

		dld.s2 = Integer.valueOf(qtxx.getString("kzsb-xhw2"));

		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		// 型号开关
		if (dwkg1 != null) {
			dld.deviceSignal1 = (DeviceSignal) servletContext.getAttribute(dwkg1 + "_" + Device.KEY);
		}
		if (dwkg2 != null) {
			dld.deviceSignal2 = (DeviceSignal) servletContext.getAttribute(dwkg2 + "_" + Device.KEY);
		}
		
		kw = (String)qtxx.get("kw");
		if(kw==null){
			kw="L";
		}

		dld.kwfx = (Integer) qtxx.get("sz-ssfx");

		dld.setDeviceLight(this);
	}

	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow) throws Exception {
		// TODO Auto-generated method stub
	}

}
