package com.xs.veh.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.xs.common.exception.SystemException;
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
	
	private VehCheckLogin vehCheckLogin;
	

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}

	@Autowired
	private ServletContext servletContext;

	private DeviceDisplay display;
	
	private DeviceVolume deviceVolume;
	
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

	@Value("${czpypdFlag}")
	private boolean czpypdFlag;

	
	

	public DeviceVolume getDeviceVolume() {
		return deviceVolume;
	}

	public void setDeviceVolume(DeviceVolume deviceVolume) {
		this.deviceVolume = deviceVolume;
	}

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
	 * @throws SystemException 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vheFlows,Map<String,Object> otherParam) throws IOException, InterruptedException, SystemException {
		
		this.vehCheckLogin=vehCheckLogin;
		
		dld.createNewList();
		
		Date kssj=new Date();
		
		List<LightData> datas = dld.startCheck(vehCheckLogin, vheFlows);
		
		if(dld.getKssj()!=null) {
			kssj=dld.getKssj();
		}
		
		String jg = (datas == null || datas.size() == 0) ? "X" : "O";
		String strgq="";
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
			
//			if(!czpypdFlag){
//				data.setCzpypd(LightData.PDJG_WJ);
//			}
			
			data.setZpd();
			if (data.getZpd() == CheckDataManager.PDJG_BHG) {
				jg = "X";
			}
			
			if(data.getGx()==LightData.GX_YGD){
				strgq+=data.getJyxm()+":"+data.getGq();
			}
		}
		display.sendMessage(vehCheckLogin.getHphm() + "检测完成", DeviceDisplay.SP);
		display.sendMessage(strgq, DeviceDisplay.XP);

		// 灯光检测完成后等待8秒 仪器归为时间
		Thread.sleep(3000);
		display.sendMessage(vehCheckLogin.getHphm() + "检测完成", DeviceDisplay.SP);
		display.sendMessage("判定结果：" + jg, DeviceDisplay.XP);
		Thread.sleep(4000);
		display.sendMessage(vehCheckLogin.getHphm() + "检测完成", DeviceDisplay.SP);
		display.sendMessage("请向前行驶", DeviceDisplay.XP);
		// 判定车是否离开 如果没有离开，则等待是否复位 ，如果离开则结束检测
		while (dld.deviceSignal1.getSignal(dld.s1) && !dld.deviceSignal2.getSignal(dld.s2)) {
			Thread.sleep(300);
		}
		display.setDefault();
		
		for (LightData data : datas){
			this.checkDataManager.saveData(data);
		}
		logger.info("灯光数据保存成功！");
		for(VehFlow vehFlow: vheFlows){
			logger.info("开始更新过程数据：流水号"+vehCheckLogin.getJylsh()+" 检验次数："+vehCheckLogin.getJycs());
			VehCheckProcess process = this.checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(),
					vehFlow.getJyxm());
			process.setKssj(kssj);
			process.setJssj(new Date());
			process.setJcxdh(this.getDevice().getJcxxh());
			this.checkDataManager.updateProcess(process);
			logger.info("过程数据更新成功！");
			
			VehCheckProcess vp =process;
			checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C55", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
			Thread.sleep(100);
			checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C81", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
			Thread.sleep(100);
			checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C58", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
			
		}
		
	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// 初始化灯光仪解码器
		dld = (AbstractDeviceLight) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		JSONObject qtxx = this.getQtxxObject();
		String temp = (String) qtxx.get("kzsb-xsp");
		String dwkg1 = (String) qtxx.get("kzsb-dwkg1");
		String dwkg2 = (String) qtxx.get("kzsb-dwkg2");
		
		String sjj=(String) qtxx.get("kzsb-sjj");

		dld.s1 = Integer.valueOf(qtxx.getString("kzsb-xhw1"));

		dld.s2 = Integer.valueOf(qtxx.getString("kzsb-xhw2"));
		
		if(!StringUtils.isEmpty(sjj)) {
			logger.info("初始化声级计："+sjj);
			Integer deviceid = Integer.parseInt(sjj);
			deviceVolume = (DeviceVolume) servletContext.getAttribute(deviceid + "_" + Device.KEY);
			logger.info("声级计："+deviceVolume);
		}

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

	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow,Map<String,Object> otherParam){
		// TODO Auto-generated method stub
	}

	@Override
	public String getDeviceSpringName() {
		return "deviceLight";
	}

}
