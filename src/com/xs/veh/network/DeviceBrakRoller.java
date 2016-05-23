package com.xs.veh.network;

import java.io.IOException;
import java.util.TooManyListenersException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.xs.common.CharUtil;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.BrakRollerData;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;

@Service("deviceBrakRoller")
@Scope("prototype")
public class DeviceBrakRoller extends SimpleRead implements ICheckDevice {

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

	private BrakRollerData brakRollerData;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executor;

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

	public BrakRollerData getBrakRollerData() {
		return brakRollerData;
	}

	public void setBrakRollerData(BrakRollerData brakRollerData) {
		this.brakRollerData = brakRollerData;
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
		/*try {
			System.out.println("制动开始");
			startCheck();
		} catch (SystemException | IOException | InterruptedException e) {
			e.printStackTrace();
		}*/
	}

	public synchronized void startCheck(VehCheckLogin vehCheckLogin,VehFlow vehFlow) throws SystemException, IOException, InterruptedException {
		
		BrakRollerData brakRollerData = dbrd.startCheck(vehFlow);
		this.checkDataManager.saveBrakRoller(brakRollerData);
		while(this.getSignal()){
			Thread.sleep(200);
			//等待是否复位
			
		}
	}

	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// 初始化制动设备
		dbrd = (AbstractDeviceBrakRoller) Class.forName(this.getDevice().getDeviceDecode()).newInstance();
		
		String dwkg = (String) this.getQtxxObject().get("kzsb-dwkg");
		
		s1=this.getQtxxObject().getInt("kzsb-xhw");

		String temp = (String) this.getQtxxObject().get("kzsb-xsp");
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		
		if(dwkg!=null){
			signal=(DeviceSignal)servletContext.getAttribute(dwkg + "_" + Device.KEY);
		}
		
		dbrd.init(this);
	}

}
