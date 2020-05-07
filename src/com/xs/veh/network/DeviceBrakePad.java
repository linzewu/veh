package com.xs.veh.network;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.OtherInfoData;
import com.xs.veh.network.data.ParDataOfAnjian;

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

	private VehCheckLogin vehCheckLogin;

	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	private CheckEventManger checkEventManger;

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}

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

	public DeviceBrakePad() {

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
	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow, Map<String, Object> otherParam) {
		// TODO Auto-generated method stub
	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows, Map<String, Object> otherParam)
			throws InterruptedException, IOException {

		this.vehCheckLogin = vehCheckLogin;

		Date startDate = new Date();
		DecimalFormat decimalFormat = new DecimalFormat(".0");
		List<BrakRollerData> datas = dbp.startCheck(vehFlows);
		Calendar calendar = Calendar.getInstance();
		// 驻车结果
		ParDataOfAnjian parDataOfAnjian = null;
		OtherInfoData otherInfoData = new OtherInfoData();
		otherInfoData.setBaseInfo(vehCheckLogin);
		Integer zclh = 0;
		Integer zdlh = 0;
		boolean sfhg = true;
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
				brakRollerData.setKzbphl(vehCheckLogin);
				// 设置不平衡率限值
				brakRollerData.setBphlxz(vehCheckLogin);
				// 空载不平衡率判定
				brakRollerData.setKzbphlpd();
				brakRollerData.setZpd();

				String strpd = "O";

				if (brakRollerData.getZpd() == BrakRollerData.PDJG_BHG) {
					sfhg = false;
					strpd = "X";
				}
				display.sendMessage(
						brakRollerData.getZw() + "轴：" + decimalFormat.format(brakRollerData.getKzxczdl()) + "/"
								+ decimalFormat.format(brakRollerData.getKzbphl()) + "/" + strpd,
						brakRollerData.getZw() == 1 ? DeviceDisplay.XP : DeviceDisplay.XP);
				
				zdlh += brakRollerData.getZzdl() + brakRollerData.getYzdl();
				zclh += brakRollerData.getZlh() + brakRollerData.getYlh();
				Thread.sleep(10000);
			} else {
				parDataOfAnjian = new ParDataOfAnjian();
				parDataOfAnjian.setZczczdl(brakRollerData.getZzdl() + brakRollerData.getYzdl());
			}
		}

		//Thread.sleep(10000);

		if (parDataOfAnjian != null) {
			logger.info("驻车判定！");
			Integer zczczdl = parDataOfAnjian.getZczczdl();
			zczczdl = zczczdl == null ? 0 : zczczdl;
			
			Integer oldzclh=(Integer) otherParam.get("zclh");
			zclh=(zclh==0&&oldzclh!=null)?oldzclh:zclh;
			
			logger.info("驻车轮荷："+zclh);
			logger.info("驻车制动力："+parDataOfAnjian.getZczczdl());
			if(zclh>0){
				parDataOfAnjian.setTczclh(zclh);
				Float tczdl = (float) ((parDataOfAnjian.getZczczdl() * 1.0 / (zclh * 0.98 * 1.0)) * 100);
				logger.info("驻车制动率："+CheckDataManager.MathRound1(tczdl));
				parDataOfAnjian.setTczclh(zclh);
				parDataOfAnjian.setTczdl(CheckDataManager.MathRound1(tczdl));
				parDataOfAnjian.setTczdxz(vehCheckLogin,false);
				parDataOfAnjian.setTczdpd();
				String strpd = "O";
				if (parDataOfAnjian.getTczdpd() == BrakRollerData.PDJG_BHG) {
					logger.info("驻车判定不合格！");
					sfhg = false;
					strpd = "X";
				}
				display.sendMessage("驻车：" + decimalFormat.format(CheckDataManager.MathRound1(tczdl)) + "/" + strpd,
						DeviceDisplay.SP);
			}
			
			
		}else{
			display.sendMessage(vehCheckLogin.getHphm(),DeviceDisplay.SP);
		}

		
		if(zdlh>0){
			
			List<String> notjyxm = notJYXM(datas);
			
			for(String j:notjyxm) {
				BrakRollerData b =this.checkDataManager.getLastBrakRollerDataOfVehLoginInfo(vehCheckLogin, j);
				zdlh += b.getZzdl() + b.getYzdl();
				zclh += b.getZlh() + b.getYlh();
			}
			
			otherInfoData.setJczczbzl(zclh);
			otherInfoData.setZdlh(zdlh);
			if (zclh != 0) {
				Float zczdl = (float) ((zdlh * 1.0 / (zclh * 0.98 * 1.0)) * 100);
				otherInfoData.setZczdl(CheckDataManager.MathRound1(zczdl));
			}
			otherInfoData.setZczdlxz();
			otherInfoData.setZczdlpd();
			String strpd = "O";
			if (otherInfoData.getZcpd().equals(BrakRollerData.PDJG_BHG.toString())) {
				logger.info("整车判定不合格！");
				strpd="X";
				sfhg = false;
			}
			display.sendMessage("整车：" + decimalFormat.format(otherInfoData.getZczdl()) + "/" + strpd,
					DeviceDisplay.XP);
			Thread.sleep(2000);
		}
		
		
		if (sfhg) {
			display.sendMessage("检判定结果：O", DeviceDisplay.XP);
		} else {
			display.sendMessage("检判定结果：X", DeviceDisplay.SP);
			//display.sendMessage("是否复位，等待20S", DeviceDisplay.XP); 
			//Thread.sleep(20 * 1000);
		}

		Thread.sleep(2000);
		display.sendMessage("请向前行驶", DeviceDisplay.XP);

		Thread.sleep(2000);
		this.display.setDefault();

		for (BrakRollerData brakRollerData : datas) {
			this.checkDataManager.saveData(brakRollerData);
		}
		
		
		this.checkDataManager.getBrakRollerDataB0(vehCheckLogin);
		
		
		
		for (VehFlow vehFlow : vehFlows) {
			VehCheckProcess process = this.checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(),
					vehCheckLogin.getJycs(), vehFlow.getJyxm());
			
			if(parDataOfAnjian==null) {
				calendar.set(Calendar.SECOND, -10);
				process.setKssj(calendar.getTime());
			}else {
				calendar.set(Calendar.SECOND, -15);
				process.setKssj(calendar.getTime());
			}
			
			process.setJssj(new Date());
			this.checkDataManager.updateProcess(process);
			
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
	
	private List<String> notJYXM(List<BrakRollerData> datas) {
		Map<String,String> jyxmMap=new HashMap<String,String>();
		for (BrakRollerData brakRollerData : datas) {
			jyxmMap.put(brakRollerData.getJyxm(), brakRollerData.getJyxm());
		}
		
		List<String> jyxmList =new ArrayList<String>();
		
		if(!jyxmMap.containsKey("B1")) {
			jyxmList.add("B1");
		}
		
		if(!jyxmMap.containsKey("B2")) {
			jyxmList.add("B2");
		}
		
		return jyxmList;
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

	@Override
	public String getDeviceSpringName() {
		return "deviceBrakePad";
	}

}
