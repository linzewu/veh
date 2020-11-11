package com.xs.veh.network;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.manager.WorkPointManager;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.ParDataOfAnjian;
import com.xs.veh.network.driver.DeviceBrakRollerDriverOfJXGT2CZ;

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
	
	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;

	@Value("${plusLoadFlag}")
	private boolean plusLoadFlag;

	private DeviceSignal signal;

	private Integer s1;

	public boolean getSignal() throws IOException, InterruptedException {
		
		if (dbrd instanceof DeviceBrakRollerDriverOfJXGT2CZ) {
			DeviceBrakRollerDriverOfJXGT2CZ dbrdcz=(DeviceBrakRollerDriverOfJXGT2CZ)dbrd;
			
			return dbrdcz.getSignal(1);
		};
		
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
		
		if(intZw==0){
			intZw=Integer.parseInt(vehFlow.getMemo());
		}
		
		int nexzw=-1;
		if(nextVehFlow!=null){
			if(nextVehFlow.getJyxm().indexOf("B")==0||nextVehFlow.getJyxm().indexOf("L")==0) {
				nexzw=Integer.parseInt(nextVehFlow.getJyxm().substring(1,2));
				if(nexzw==0){
					nexzw =Integer.parseInt(nextVehFlow.getMemo());
				}
			}
			
		}
		
		logger.info("intZw:" + intZw);
		logger.info("nexzw:" + nexzw);
		
		BrakRollerData brakRollerData = (BrakRollerData) otherParam.get("brakRollerData");

		logger.info("brakRollerData:" + brakRollerData);

		if (brakRollerData == null) {
			dbrd.setBrakRollerData(new BrakRollerData());
		} else {
			dbrd.setBrakRollerData(brakRollerData);
		}

	//	Date kssj = new Date();

		brakRollerData = dbrd.startCheck(vehFlow);
		
		//如果检测结果制动力为空 则检测异常
		if(brakRollerData.getZzdl()==null||brakRollerData.getYzdl()==null){
			display.sendMessage("异常，5秒后重检", DeviceDisplay.SP);
			display.sendMessage("获取不到制动力", DeviceDisplay.XP);
			Thread.sleep(5000);
			throw new SystemException("获取不到制动力");
		}
		to300Point(brakRollerData);
		// setInfoData(brakRollerData);
		// 设置基础数据
		brakRollerData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), vehFlow.getJyxm());

		// 非驻车制动则计算检测结果
		if (!brakRollerData.getJyxm().equals("B0")) {
			// 空载行车制动率
			brakRollerData.setKzxczdl(vehCheckLogin);
			// 空载制动率限制及判定
			brakRollerData.setKzzdlxz(vehCheckLogin);
			brakRollerData.setKzzdlpd(vehCheckLogin);

			// 设置空载不平衡率
			brakRollerData.setKzbphl(vehCheckLogin);
			// 设置不平衡率限值
			brakRollerData.setBphlxz(vehCheckLogin);
			// 空载不平衡率判定
			brakRollerData.setKzbphlpd();
//
//			brakRollerData.setJzzdl();
//			// 加载制动率限制及判定
//			brakRollerData.setJzzdlxz(vehCheckLogin);
//			brakRollerData.setJzzdlpd();
//
//			// 设置加载不平衡率
//			brakRollerData.setJzbphl(vehCheckLogin);
//			// 加载不平衡率判定
//			brakRollerData.setJzbphlpd();
			brakRollerData.setZpd();
			
		}
		
		

		if (brakRollerData.getKzxczdl() != null) {
			DecimalFormat decimalFormat = new DecimalFormat(".0");
			display.sendMessage("左："+brakRollerData.getZzdl()+" 右："+brakRollerData.getYzdl()+"/"+decimalFormat.format(brakRollerData.getKzxczdl()) + "/"
					+ decimalFormat.format(brakRollerData.getKzbphl()), DeviceDisplay.XP);
		}
		

		if (nextVehFlow != null && nexzw==intZw) {
			if (brakRollerData.getZpd() == BrakRollerData.PDJG_HG) {
				display.sendMessage("检判定结果：O", DeviceDisplay.SP);
				Thread.sleep(5000);
			} else {
				display.sendMessage("检判定结果：X", DeviceDisplay.SP);
				Thread.sleep(5000);
				String zczw = vehCheckLogin.getZczw();
				Integer maxzw = this.getMaxZw(zczw);
				if(!vehFlow.getJyxm().equals("B0")||maxzw!=brakRollerData.getZw()) {
					display.sendMessage("等待是否复位,12秒", DeviceDisplay.XP);
					Thread.sleep(12000);
				}
			}
		} else {
			if (!vehFlow.getJyxm().equals("B0")) {
				if (brakRollerData.getZpd() == BrakRollerData.PDJG_HG) {
					display.sendMessage("检判定结果：O", DeviceDisplay.SP);
					Thread.sleep(5000);
				} else {
					display.sendMessage("检判定结果：X", DeviceDisplay.SP);
					Thread.sleep(5000);
				}
			}else{
				String zczw = vehCheckLogin.getZczw();
				Integer maxzw = this.getMaxZw(zczw);
				List<BrakRollerData> b0s = (List<BrakRollerData>) otherParam.get("B0");
				ParDataOfAnjian parDataOfAnjian=new ParDataOfAnjian();
				
				if(b0s!=null&&!b0s.isEmpty()){
					for(BrakRollerData brd:b0s){
						Integer zczczdl = parDataOfAnjian.getZczczdl();
						zczczdl = zczczdl == null ? 0 : zczczdl;
						parDataOfAnjian.setZczczdl(zczczdl + brd.getZzdl() + brd.getYzdl());
					}
				}
				Integer zclh =(Integer) otherParam.get("zclh");
				Integer zczczdl = parDataOfAnjian.getZczczdl();
				zczczdl = zczczdl == null ? 0 : zczczdl;
				parDataOfAnjian.setZczczdl(zczczdl + brakRollerData.getZzdl() + brakRollerData.getYzdl());
				parDataOfAnjian.setTczclh(zclh);
				Float tczdl = (float) ((parDataOfAnjian.getZczczdl() * 1.0 / (zclh * 0.98 * 1.0)) * 100);
				
				display.sendMessage(parDataOfAnjian.getZczczdl()+"/"+CheckDataManager.MathRound1(tczdl), DeviceDisplay.SP);
				logger.info("驻车最大轴位："+maxzw  + "当前轴位："+brakRollerData.getZw() +"   "+(maxzw==brakRollerData.getZw()));
				if(maxzw==brakRollerData.getZw()){
					parDataOfAnjian.setTczclh(zclh);
					parDataOfAnjian.setTczdl(CheckDataManager.MathRound1(tczdl));
					parDataOfAnjian.setTczdxz(vehCheckLogin,true);
					parDataOfAnjian.setTczdpd();
					if (parDataOfAnjian.getTczdpd() == BrakRollerData.PDJG_HG) {
						display.sendMessage("检判定结果：O", DeviceDisplay.XP);
						Thread.sleep(1500);
					} else {
						display.sendMessage("检判定结果：X", DeviceDisplay.XP);
						Thread.sleep(5000);
					}
				}
			}
			display.sendMessage("请向前行驶", DeviceDisplay.XP);
		}
		
		if (nexzw != -1 &&nexzw==intZw ) {
			String mes ="";
			if(nextVehFlow.getJyxm().equals("B0")){
				mes ="请等待，检测驻车！";
			}else{
				mes ="请等待，检测加载！";
			}
			display.sendMessage(mes, DeviceDisplay.XP);
			Thread.sleep(1000);
		}else {
			while (this.getSignal()) {
				Thread.sleep(500);
			}
			this.display.setDefault();
		}
		VehCheckProcess process = this.checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(),
				vehCheckLogin.getJycs(), vehFlow.getJyxm());
		
//		if(process.getJyxm().equals("B0")&&process.getKssj()!=null&&process.getJssj()!=null) {
//			VehCheckProcess  vcp=new VehCheckProcess();
//			vcp.setClsbdh(process.getClsbdh());
//			vcp.setHphm(process.getHphm());
//			vcp.setHpzl(process.getHpzl());
//			vcp.setJcxdh(process.getJcxdh());
//			vcp.setJssj(process.getJssj());
//			vcp.setJygcxrsj(process.getJygcxrsj());
//			vcp.setJycs(process.getJycs());
//			vcp.setJylsh(process.getJylsh());
//			vcp.setJysbbh(process.getJysbbh());
//			vcp.setJyxm(process.getJyxm()+"-"+(Integer.parseInt(vehFlow.getMemo())-1));
//			vcp.setKssj(process.getKssj());
//			vcp.setStatus(0);
//			vcp.setVoideSate(0);
//			this.checkDataManager.saveProcess(vcp);
//		}
		
		process.setJssj(new Date());
		if(brakRollerData.getGckssj()!=null) {
			process.setKssj(brakRollerData.getGckssj());
		}else {
			process.setKssj(dbrd.dwKssj);
		}
		
		process.setJcxdh(this.getDevice().getJcxxh());
		
		this.checkDataManager.updateProcess(process);
		
		
		
		this.checkDataManager.saveData(brakRollerData);
		
		Thread.sleep(200);
		VehCheckProcess vp =process;
		
		if(!vehFlow.getJyxm().equals("B0")) {
			checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C55", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
			checkEventManger.createEvent(500,vp.getJylsh(), vp.getJycs(), "18C81", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
			checkEventManger.createEvent(800,vp.getJylsh(), vp.getJycs(), "18C58", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
		}else {
			logger.info("B0驻车过程不发送数据。");
		}
		
		if(nextVehFlow==null||(nextVehFlow.getJyxm().indexOf("B")==-1&&nextVehFlow.getJyxm().indexOf("L")==-1)) {
			this.checkDataManager.createParDataOfAnjian(vehCheckLogin.getJylsh());
		}
		
	}

	public static void to300Point(BrakRollerData brakRollerData) {
		String leftStr = brakRollerData.getLeftDataStr();
		if(leftStr!=null) {
			String[] leftArray = leftStr.split(",");
			if(leftArray.length<300) {
				List<String> leftList =new ArrayList<String>();
				CollectionUtils.addAll(leftList,leftArray);
				while(leftList.size()<300) {
					leftList.add("0");
				}
				String newLeftStr = StringUtils.join(leftList,",");
				brakRollerData.setLeftDataStr(newLeftStr);
			}
		}
		String rigthStr = brakRollerData.getRigthDataStr();
		if(rigthStr!=null) {
			String[] rigthtArray = rigthStr.split(",");
			if(rigthtArray.length<300) {
				List<String> rigthList =new ArrayList<String>();
				CollectionUtils.addAll(rigthList,rigthtArray);
				while(rigthList.size()<300) {
					rigthList.add("0");
				}
				String newrigthStr = StringUtils.join(rigthList,",");
				brakRollerData.setRigthDataStr(newrigthStr);
			}
		}
	}
	
	public static void main(String[] age) {
		
		BrakRollerData b=new BrakRollerData();
		
		b.setLeftDataStr("1");
		b.setRigthDataStr("2");
		
		to300Point(b);
		
		System.out.println(b.getLeftDataStr());
		
		System.out.println(b.getRigthDataStr());
		
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

	
	private Integer getMaxZw(String zw){
		char[] zws = zw.toCharArray();
		char max='0';
		for(char c:zws){
			if(c>max){
				max=c;
			}
		}
		return Integer.parseInt(String.valueOf(max));
	}

	@Override
	public String getDeviceSpringName() {
		return "deviceBrakRoller";
	}
	
}
