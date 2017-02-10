package com.xs.veh.network;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.WorkPointManager;
import com.xs.veh.network.data.BrakRollerData;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;

@Service("deviceBrakRoller")
@Scope("prototype")
public class DeviceBrakRoller extends SimpleRead implements ICheckDevice {

	private VehCheckLogin vehCheckLogin;

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}

	public DeviceBrakRoller() {
	}

	public enum BrakRollerDataType {
		R_DATA, L_DATA, RESULT_DATA
	}

	private static Logger logger = Logger.getLogger(DeviceBrakRoller.class);

	private AbstractDeviceBrakRoller dbrd;

	private DeviceDisplay display;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "workPointManager")
	private WorkPointManager workPointManager;

	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executor;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Value("${plusLoadFlag}")
	private boolean plusLoadFlag;

	private DeviceSignal signal;

	private Integer s1;

	public boolean getSignal() {

		return this.signal.getSignal(s1);
	}

	public ThreadPoolTaskExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(ThreadPoolTaskExecutor executor) {
		this.executor = executor;
	}

	public DeviceDisplay getDisplay() {
		return display;
	}

	public void setDisplay(DeviceDisplay display) {
		this.display = display;
	}

	public CheckDataManager getCheckDataManager() {
		return checkDataManager;
	}

	public void setCheckDataManager(CheckDataManager checkDataManager) {
		this.checkDataManager = checkDataManager;
	}

	public DeviceBrakRoller(Device device)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchPortException,
			TooManyListenersException, PortInUseException, UnsupportedCommOperationException, IOException {
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

				dbrd.device2pc(endodedData);

			} catch (Exception e) {
				logger.error("制动设备获取数据异常", e);
			}
			break;
		}
	}

	@Override
	public void run() {
	}

	public synchronized void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow, Map<String, Object> otherParam)
			throws IOException, InterruptedException, SystemException {

		this.vehCheckLogin = vehCheckLogin;

		dbrd.resetCheckStatus();

		VehFlow nextVehFlow = workPointManager.getNextFlow(vehFlow);
		dbrd.setNextVehFlow(nextVehFlow);
		Integer intZw = Integer.parseInt(vehFlow.getJyxm().substring(1, 2));

		logger.info("vehCheckLogin.getZs()" + vehCheckLogin.getZs());
		logger.info("vehCheckLogin.getCllx()" + vehCheckLogin.getCllx());
		logger.info("intZw" + intZw);

		if (vehCheckLogin.getZs() >= 3
				&& (vehCheckLogin.getCllx().indexOf("G") == 0 || vehCheckLogin.getCllx().indexOf("B") == 0)
				&& vehCheckLogin.getZs() != intZw && intZw != 0 && plusLoadFlag) {
			dbrd.isPlusLoad = true;
		} else if (vehCheckLogin.getCllx().indexOf("H") == 0 && vehCheckLogin.getZs() >= 3 && intZw > 1
				&& vehCheckLogin.getZs() != intZw && intZw != 0 && plusLoadFlag) {
			dbrd.isPlusLoad = true;
		} else {
			dbrd.isPlusLoad = false;
		}
		logger.info("dbrd.isPlusLoad:" + dbrd.isPlusLoad);

		BrakRollerData brakRollerData = (BrakRollerData) otherParam.get("brakRollerData");

		logger.info("brakRollerData:" + brakRollerData);

		if (brakRollerData == null) {
			dbrd.setBrakRollerData(new BrakRollerData());
		} else {
			dbrd.setBrakRollerData(brakRollerData);
		}

		Date kssj = new Date();

		brakRollerData = dbrd.startCheck(vehFlow);
		
		//如果检测结果制动力为空 则检测异常
		if(brakRollerData.getZzdl()==null||brakRollerData.getYzdl()==null){
			display.sendMessage("异常，5秒后重检", DeviceDisplay.SP);
			display.sendMessage("获取不到制动力", DeviceDisplay.XP);
			Thread.sleep(5000);
			throw new SystemException("获取不到制动力");
		}

		// setInfoData(brakRollerData);

		// 设置基础数据
		brakRollerData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), vehFlow.getJyxm());

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

			brakRollerData.setJzzdl();
			// 加载制动率限制及判定
			brakRollerData.setJzzdlxz(vehCheckLogin);
			brakRollerData.setJzzdlpd();

			// 设置加载不平衡率
			brakRollerData.setJzbphl(vehCheckLogin);
			// 加载不平衡率判定
			brakRollerData.setJzbphlpd();
			brakRollerData.setZpd();
		}

		if (brakRollerData.getKzxczdl() != null) {
			DecimalFormat decimalFormat = new DecimalFormat(".0");
			display.sendMessage(decimalFormat.format(brakRollerData.getKzxczdl()) + "/"
					+ decimalFormat.format(brakRollerData.getKzbphl()), DeviceDisplay.SP);
		}

		if (nextVehFlow != null && nextVehFlow.getJyxm().equals("B0")) {
			if (brakRollerData.getZpd() == BrakRollerData.PDJG_HG) {
				display.sendMessage("检判定结果：O", DeviceDisplay.XP);
				Thread.sleep(2000);
			} else {
				display.sendMessage("检判定结果：X", DeviceDisplay.XP);
				Thread.sleep(5000);
				display.sendMessage("等待是否复位,20秒", DeviceDisplay.XP);
				// 不合格等待15秒
				Thread.sleep(20000);
			}
		} else {
			if (!vehFlow.getJyxm().equals("B0")) {
				if (brakRollerData.getZpd() == BrakRollerData.PDJG_HG) {
					display.sendMessage("检判定结果：O", DeviceDisplay.XP);
				} else {
					display.sendMessage("检判定结果：X", DeviceDisplay.XP);
				}
				Thread.sleep(1500);
			}
			display.sendMessage("请向前行驶", DeviceDisplay.XP);
		}

		if (nextVehFlow != null && nextVehFlow.getJyxm().equals("B0") && !vehFlow.getJyxm().equals("B0")) {
			display.sendMessage("请等待，检测驻车！", DeviceDisplay.XP);
			Thread.sleep(1000);
		} else {
			while (this.getSignal()) {
				Thread.sleep(500);
			}
			this.display.setDefault();
		}
		VehCheckProcess process = this.checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(),
				vehCheckLogin.getJycs(), vehFlow.getJyxm());
		process.setJssj(new Date());
		process.setKssj(kssj);
		this.checkDataManager.updateProcess(process);
		this.checkDataManager.saveData(brakRollerData);
	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// 初始化制动设备
		dbrd = (AbstractDeviceBrakRoller) Class.forName(this.getDevice().getDeviceDecode()).newInstance();

		String dwkg = (String) this.getQtxxObject().get("kzsb-dwkg");

		s1 = this.getQtxxObject().getInt("kzsb-xhw");

		String temp = (String) this.getQtxxObject().get("kzsb-xsp");
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}

		if (dwkg != null) {
			signal = (DeviceSignal) servletContext.getAttribute(dwkg + "_" + Device.KEY);
		}
		dbrd.init(this);
	}

	public void setInfoData(BrakRollerData brakRollerData) {
		if (brakRollerData.getLeftData() != null && !brakRollerData.getLeftData().isEmpty()) {
			StringBuffer leftDataStr = new StringBuffer();

			for (Integer strData : brakRollerData.getLeftData()) {
				leftDataStr.append("," + strData);
			}
			if (leftDataStr.length() > 0) {
				leftDataStr.substring(1);
			}
			brakRollerData.setLeftDataStr(leftDataStr.toString());
		}

		if (brakRollerData.getRigthData() != null && !brakRollerData.getRigthData().isEmpty()) {
			StringBuffer rigthDataStr = new StringBuffer();

			for (Integer strData : brakRollerData.getRigthData()) {
				rigthDataStr.append("," + strData);
			}
			if (rigthDataStr.length() > 0) {
				rigthDataStr.substring(1);
			}
			brakRollerData.setRigthDataStr(rigthDataStr.toString());
		}
	}

	public void setJZInfoData(BrakRollerData brakRollerData) {
		if (brakRollerData.getLeftData() != null && !brakRollerData.getLeftData().isEmpty()) {
			StringBuffer leftDataStr = new StringBuffer();
			for (Integer strData : brakRollerData.getLeftData()) {
				leftDataStr.append("," + strData);
			}
			if (leftDataStr.length() > 0) {
				leftDataStr.substring(1);
			}
			brakRollerData.setJzLeftDataStr(leftDataStr.toString());
		}

		if (brakRollerData.getRigthData() != null && !brakRollerData.getRigthData().isEmpty()) {
			StringBuffer rigthDataStr = new StringBuffer();

			for (Integer strData : brakRollerData.getRigthData()) {
				rigthDataStr.append("," + strData);
			}
			if (rigthDataStr.length() > 0) {
				rigthDataStr.substring(1);
			}
			brakRollerData.setJzRigthDataStr(rigthDataStr.toString());
		}
	}

	@Override
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows, Map<String, Object> otherParam) {
		// TODO Auto-generated method stub

	}

}
