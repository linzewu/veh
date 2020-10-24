package com.xs.veh.network;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.manager.VehManager;
import com.xs.veh.manager.VehProcessManager;
import com.xs.veh.network.data.BaseDeviceData;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.CurbWeightData;
import com.xs.veh.network.driver.DeviceWeighDriverOfJXZB10_SZ;

import gnu.io.SerialPortEvent;

/**
 * 
 * @author linze
 *
 */
@Service("deviceWeigh")
@Scope("prototype")
public class DeviceWeigh extends SimpleRead implements ICheckDevice {

	private AbstractDeviceWeigh dw;

	private DeviceDisplay display;

	private DeviceSignal signal;

	private Integer s1;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executor;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private VehManager vehManager;
	
	@Autowired
	private VehProcessManager vehProcessManager;
	@Autowired
	private CheckEventManger checkEventManger;
	
	private VehCheckLogin vehCheckLogin;
	
	
	
	

	public CheckEventManger getCheckEventManger() {
		return checkEventManger;
	}

	public void setCheckEventManger(CheckEventManger checkEventManger) {
		this.checkEventManger = checkEventManger;
	}

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}

	public Integer getS1() {
		return s1;
	}

	public void setS1(Integer s1) {
		this.s1 = s1;
	}

	public DeviceDisplay getDisplay() {
		return display;
	}

	public DeviceSignal getSignal() {
		return signal;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
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
					if (length >= 1024 * 128) {
						logger.error("读入的数据超过1024 * 128");
						break;
					}
				}
				byte[] endodedData = new byte[length];
				// logger.info("数据长度："+endodedData.length);
				System.arraycopy(readBuffer, 0, endodedData, 0, length);
				dw.device2pc(endodedData);
			} catch (Exception e) {
				logger.error("称重台通讯异常", e);
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
		s1 = getQtxxObject().getInt("kzsb-xhw");

		dw = (AbstractDeviceWeigh) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		if (dwkg != null) {
			signal = (DeviceSignal) servletContext.getAttribute(dwkg + "_" + Device.KEY);
		}
		dw.init(this);
	}

	/**
	 * 称重
	 * 
	 * @param vehCheckLogin
	 * @param vehFlow
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SystemException 
	 */
	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow,Map<String,Object> otherParam) throws IOException, InterruptedException, SystemException {
		
		this.vehCheckLogin=vehCheckLogin;
		dw.getTemp().clear();
		BrakRollerData brakRollerData = dw.startCheck(vehFlow);
		brakRollerData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), vehFlow.getJyxm());

		Thread.sleep(2000);
		this.display.sendMessage("检测完毕向前行驶", DeviceDisplay.XP);
		boolean flag = true;

		while (flag) {
			flag = this.signal.getSignal(s1);
			Thread.sleep(200);
		}
		this.checkDataManager.saveData(brakRollerData);
		display.setDefault();
		
		if(dw instanceof DeviceWeighDriverOfJXZB10_SZ) {
			
			String zs = vehFlow.getJyxm().substring(1, 2);
			
			if(vehCheckLogin.getJycs()==1&&vehCheckLogin.getJyxm().indexOf("Z1")!=-1) {
				if(zs.equals("2")) {
					this.saveZ1(brakRollerData);
				}
				
			}
			
			
		}
		
	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows,Map<String,Object> otherParam) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDeviceSpringName() {
		return "deviceWeigh";
	}
	
	
	public VehCheckProcess getZ1Process() {
		return checkDataManager.getVehCheckProces(this.vehCheckLogin.getJylsh(),this.vehCheckLogin.getJycs(), "Z1");
	}
	
	public void updateVehCheckProcess(VehCheckProcess vehCheckProcess) {
		this.checkDataManager.updateProcess(vehCheckProcess);
	}
	
	public void updateZ1VehCheckProcessStart() {
		VehCheckProcess vp =getZ1Process();
		vp.setKssj(new Date());
		this.vehProcessManager.saveVehProcessSync(vp);
		
		checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C55", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
				vp.getClsbdh(),vehCheckLogin.getVehcsbj());
	}
	
	public void saveZ1(BrakRollerData b2) throws InterruptedException {
		
		BrakRollerData b1 = this.checkDataManager.getBrakRollerDataOfVehLoginInfo(vehCheckLogin, "B1");
		
		String cllx=vehCheckLogin.getCllx();
		
		CurbWeightData curbWeightData=new CurbWeightData();
		
		curbWeightData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), "Z1");
		
		Integer qz=null;
		Integer hz=null;
		
		qz=b1.getZlh()+b1.getYlh();
		 hz=b2.getZlh()+b2.getYlh();
	
		 
		
		curbWeightData.setQzzl(qz);
		
		curbWeightData.setHzzl(hz);
		
		curbWeightData.setZbzl((qz+hz)-65);
		
		
		int xzgj=100;
		String temp1="±3%或±";
		Float temp2=0.03f;
		
		if(cllx.indexOf("H1")==0||cllx.indexOf("H2")==0||cllx.indexOf("Z1")==0||cllx.indexOf("Z2")==0||cllx.indexOf("Z5")==0||cllx.indexOf("G")==0||cllx.indexOf("B")==0){
			xzgj=500;
		}else if(cllx.indexOf("H3")==0||cllx.indexOf("H4")==0||cllx.indexOf("Z3")==0||cllx.indexOf("Z4")==0){
			xzgj=100;
		}else if(cllx.indexOf("N")==0){
			xzgj=100;
			temp2=0.05f;
			temp1="±5%或±";
		}else if(cllx.indexOf("M")==0){
			xzgj=10;
		}
		Integer cz = vehCheckLogin.getZbzl()-(curbWeightData.getZbzl());
		
		Integer pd = Math.abs(cz)<xzgj?BaseDeviceData.PDJG_HG:BaseDeviceData.PDJG_BHG;
		
		Integer pd2 = Math.abs(cz*1.0/vehCheckLogin.getZbzl()*1.0)>temp2?BaseDeviceData.PDJG_BHG:BaseDeviceData.PDJG_HG;
		
		
		if(pd==BaseDeviceData.PDJG_HG||pd2==BaseDeviceData.PDJG_HG) {
			curbWeightData.setZbzlpd(BaseDeviceData.PDJG_HG);
		}else {
			curbWeightData.setZbzlpd(BaseDeviceData.PDJG_BHG);
		}
		
		vehManager.saveCurbWeight(curbWeightData);

	}
	
	public void saveBrakRollerData(BrakRollerData brakRollerData) {
		this.checkDataManager.saveData(brakRollerData);
	}
	
	
	public BrakRollerData getBrakRollerData(String jyxm) {
		
		BrakRollerData brakRollerData = this.checkDataManager.getBrakRollerDataOfVehLoginInfo(vehCheckLogin, jyxm);
		return brakRollerData;
	}

}
