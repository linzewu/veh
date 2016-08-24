package com.xs.veh.network.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceBrakRoller;
import com.xs.veh.network.DeviceBrakRoller;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.WeighData;

/**
 * 江新加载制动台
 * 
 * @author linze
 *
 */
public class DeviceBrakRollerDriverOfJXFJZ extends AbstractDeviceBrakRoller {

	private static Logger logger = Logger.getLogger(DeviceBrakRollerDriverOfJXFJZ.class);

	// 启动左电机
	private String qdzdj = "FF0001EE";
	// 启动右电机
	private String qdydj = "FF0002EE";
	// 检测制动力
	private String jczdl = "FF0004EE";
	// 检测阻滞力
	private String jczzl = "FF0003EE";
	// 制动力检测结束
	private String zdjcjs = "FF0007EE";
	// 取检测结果
	private String qjcjg = "FF02EE";

	// 到位延时
	private String dwysqdsj = "FF08EE10";

	// 举升器上升
	private String jsqss = "FF03EE";

	// 举升器下降
	private String jsqxj = "FF04EE";
	// 左轮抱死
	private String zlbs = "FF0010EE";
	// 右轮抱死
	private String ylbs = "FF0020EE";
	// 左右轮抱死
	private String zylbs = "FF0030EE";

	// 左传感器错误
	private String zcgqcw = "FF0005EE";

	// 右传感器错误
	private String ycgqcw = "FF0006EE";

	// 仪表清零
	private String ybql = "FF05EE";

	// 开始检测
	private String ksjc = "FF0AEE";
	
	//台体上升高度
	private String ttssgd="FF12EE";
	
	//台体举升
	private String ttjs="FF13EE";
	
	//台体下降
	private String ttxj="FF14EE";
	
	//开始称重
	private String kscz="FF01EE";
	
	//结束称重
	private String jscz="FF20EE";

	private String scMessage;

	private List<Byte> tempByte;

	private List<Byte> dataTempByte;
	
	private List<Byte> weighDataTempByte;

	public void setCheckedData(Byte[] data, BrakRollerData brakRollerData) {

		Integer zzzl = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[1], data[2] }));

		Integer yzzl = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[3], data[4] }));

		Integer zzdl = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[5], data[6] }));

		Integer yzdl = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[7], data[8] }));

		Integer zzdlc = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[9], data[10] }));

		Integer yzdlc = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[11], data[12] }));

		String fh = CharUtil.byte2HexOfString(new byte[] { data[13] }).substring(0, 1);

		Float gcc = Float.parseFloat(CharUtil.byte2HexOfString(new byte[] { data[14] })) / 10;

		if (fh.equals("F")) {
			gcc = -gcc;
		}
		brakRollerData.setGcc(gcc);
		brakRollerData.setZzzl(zzzl);
		brakRollerData.setYzzl(yzzl);
		brakRollerData.setZzdl(zzdl);
		brakRollerData.setYzdl(yzdl);
		brakRollerData.setZzdlcd(zzdlc);
		brakRollerData.setYzdlcd(yzdlc);
	}

	public void setCurrentData(Byte[] data) {

		List<Integer> leftData = brakRollerData.getLeftData();
		List<Integer> rigthData = brakRollerData.getRigthData();
		for (int index = 0; index < data.length; index++) {
			// 保护代码
			if (data.length - index < 4) {
				break;
			}
			//
			Integer type = CharUtil.byteToInt(data[index]);
			Integer dd = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[++index], data[++index] }));

			if (type == 0xAA) {
				leftData.add(dd);
			}
			if (type == 0xBB) {
				rigthData.add(dd);
			}
			index++;
		}
	}

	@Override
	public BrakRollerData startCheck(VehFlow vehFlow) throws Exception {

		try {
			Integer intZw = Integer.parseInt(vehFlow.getJyxm().substring(1, 2));
			scMessage = vehFlow.getJyxm().equals("B0") ? "请拉手刹" : "请踩刹车";

			String zw = getZW(intZw);
			if (ksjc == null || ybql == null || qjcjg == null) {
				throw new SystemException("滚筒制动启动错误，通讯协议不全");
			}

			tempByte.clear();
			// 仪表清0
			logger.info("仪表清0命令：" + ybql);
			deviceBrakRoller.sendMessage(ybql);
			Thread.sleep(1000);

			// 清理数据
			deviceBrakRoller.clearDate();
			brakRollerData = new BrakRollerData();

			dw(vehFlow, zw);
			
			if(isPlusLoad){
				brakRollerData.setSfjzz(BrakRollerData.SFJZZ_YES);
				//称重
				cz();
			}else{
				brakRollerData.setSfjzz(BrakRollerData.SFJZZ_NO);
			}

			// 开始检测
			deviceBrakRoller.sendMessage(ksjc);

			// 等待检测数据返回
			while (checkingFlage) {
				if (this.getBrakRollerData().getZzdl() != null && this.getBrakRollerData().getYzdl() != null) {
					break;
				}
				Thread.sleep(300);
			}

			if (isError) {
				Thread.sleep(3000);
			}

			checkingFlage = false;
			deviceBrakRoller.getDisplay().sendMessage(zw + "制动检测完成", DeviceDisplay.SP);
			if (intZw != 0) {
				brakRollerData.setZw(intZw);
			} else {
				brakRollerData.setZw(Integer.parseInt(vehFlow.getMemo()));
			}
			return brakRollerData;
		} finally {
			if (nextVehFlow!=null&&!nextVehFlow.getJyxm().equals("B0")) {
				this.deviceBrakRoller.sendMessage(jsqss);
			}
			
			if(isPlusLoad){
				this.deviceBrakRoller.sendMessage(jsqxj);
			}
		}
	}
	
	/**
	 * 称重
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void cz() throws Exception, InterruptedException, IOException {
		this.setWeighData(new WeighData());
		//举升高度命令
		deviceBrakRoller.sendMessage(ttssgd);
		Thread.sleep(300);
		
		//举升命令
		deviceBrakRoller.sendMessage(ttjs);
		
		//等待5秒 
		Thread.sleep(5000);
		
		//开始称重命令
		deviceBrakRoller.sendMessage(kscz);
		int i = 0;
		while(true){
			deviceBrakRoller.getDisplay().sendMessage((this.getWeighData().getLeftData() + this.getWeighData().getRightData()) + "KG",
					DeviceDisplay.XP);
			Thread.sleep(500);
			i++;
			if (i >= 5) {
				break;
			}
		}
		
		deviceBrakRoller.sendMessage(jscz);
		//结束称重
	}

	private void dw(VehFlow vehFlow, String zw) throws Exception, InterruptedException, IOException {
		// 发送到位延时时间
		deviceBrakRoller.sendMessage(dwysqdsj);
		Thread.sleep(200);

		deviceBrakRoller.getDisplay().sendMessage(vehFlow.getHphm(), DeviceDisplay.SP);
		deviceBrakRoller.getDisplay().sendMessage(zw + "制动请到位", DeviceDisplay.XP);

		// 等待到位
		int i = 0;
		while (true) {
			if (deviceBrakRoller.getSignal()) {
				deviceBrakRoller.getDisplay().sendMessage(vehFlow.getHphm(), DeviceDisplay.SP);
				deviceBrakRoller.getDisplay().sendMessage(zw + "已到位", DeviceDisplay.XP);
				i++;
			} else {
				deviceBrakRoller.getDisplay().sendMessage(vehFlow.getHphm(), DeviceDisplay.SP);
				deviceBrakRoller.getDisplay().sendMessage(zw + "制动请到位", DeviceDisplay.XP);
				i = 0;
			}
			if (i >= 6) {
				break;
			}
			Thread.sleep(500);
		}

		if (!vehFlow.getJyxm().equals("B0")) {
			// 举升下降
			deviceBrakRoller.sendMessage(jsqxj);
			Thread.sleep(4000);
		}
	}

	@Override
	public void device2pc(byte[] endodedData) throws Exception {

		logger.info("制动数据：" + CharUtil.byte2HexOfString(endodedData));

		setTempByte(endodedData);

	}

	// 倒数计时线程
	public void ds(final String title, final Integer ms, final String afterTitle) {

		deviceBrakRoller.getExecutor().execute(new Runnable() {
			public void run() {
				try {
					deviceBrakRoller.getDisplay().sendMessage(title, DeviceDisplay.SP);
					for (int i = 1; i <= ms; i++) {
						if (isbs) {
							return;
						}
						deviceBrakRoller.getDisplay().sendMessage(String.valueOf(i), DeviceDisplay.XP);
						Thread.sleep(1000);
					}
					if (afterTitle != null) {
						deviceBrakRoller.getDisplay().sendMessage(afterTitle, DeviceDisplay.XP);
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

	}

	private void setTempByte(byte[] endodedData) throws Exception {

		for (byte b : endodedData) {
			int i = CharUtil.byteToInt(b);

			if (i == 0xFF) {
				this.tempByte.clear();
				this.tempByte.add(b);
			} else if (i == 0xEE && this.tempByte.size() > 0) {
				this.tempByte.add(b);
				Byte[] data = this.tempByte.toArray(new Byte[tempByte.size()]);

				logger.info("日志：" + CharUtil.byte2HexOfString(data));
				if (data.length == 4) {
					process(data);
				} else if (data.length == 3) {
					logger.info("命令代码回复：" + CharUtil.byte2HexOfString(data));
				} else if (data.length == 16) {
					setCheckedData(data, brakRollerData);
				}
				this.tempByte.clear();
			}else if(i == 0xCC || i == 0xDD){
				weighDataTempByte.clear();
				weighDataTempByte.add(b);
			} else if(i == 0xEE && this.weighDataTempByte.size() > 0){
				weighDataTempByte.add(b);
				Byte[] data =this.weighDataTempByte.toArray(new Byte[weighDataTempByte.size()]);
				setWeighData(data);
				
			}else if (i == 0xAA || i == 0xBB) {
				dataTempByte.clear();
				dataTempByte.add(b);
			} else if (i == 0xEE && this.dataTempByte.size() > 0) {
				dataTempByte.add(b);
				Byte[] data = this.dataTempByte.toArray(new Byte[dataTempByte.size()]);
				setCurrentData(data);
			} else if (this.tempByte.size() > 0) {
				tempByte.add(b);
			} else if (this.dataTempByte.size() > 0) {
				dataTempByte.add(b);
			}else if(weighDataTempByte.size()>0){
				weighDataTempByte.add(b);
			}
		}
	}

	private void setWeighData(Byte[] data) {
		WeighData weighData =this.getWeighData();
		if (weighData != null && data.length == 8) {
			String t1 = CharUtil.bcd2Str(new byte[] { data[1], data[2] });
			String t2 = CharUtil.bcd2Str(new byte[] { data[5], data[6] });
			weighData.setLeftData(Integer.parseInt(t1));
			weighData.setRightData(Integer.parseInt(t2));
		}
	}

	public void process(Byte[] data) throws Exception {
		String ml = CharUtil.byte2HexOfString(data);
		logger.info("仪表返回命令：" + ml);
		if (ml.equalsIgnoreCase(qdzdj)) {
			deviceBrakRoller.getDisplay().sendMessage("检测开始", DeviceDisplay.SP);
			deviceBrakRoller.getDisplay().sendMessage("请放手刹", DeviceDisplay.XP);
			return;
		}
		if (ml.equalsIgnoreCase(jczzl)) {
			logger.info("开始检测阻滞力");
			ds("开始检测阻滞力", 3, null);
			return;
		}

		if (ml.equalsIgnoreCase(jczdl)) {
			ds(scMessage, 7, "制动力检测完成");
			return;
		}
		// 制动检测结束
		if (ml.equalsIgnoreCase(zdjcjs) || ml.equalsIgnoreCase(zlbs) || ml.equalsIgnoreCase(ylbs)
				|| ml.equalsIgnoreCase(zylbs)) {
			// 设置结束状态
			if (ml.equalsIgnoreCase(zlbs)) {
				brakRollerData.setJszt(BrakRollerData.JSZT_ZLBS);
			} else if (ml.equalsIgnoreCase(ylbs)) {
				brakRollerData.setJszt(BrakRollerData.JSZT_YLBS);
			} else if (ml.equalsIgnoreCase(zylbs)) {
				brakRollerData.setJszt(BrakRollerData.JSZT_ZYLBS);
			} else {
				brakRollerData.setJszt(BrakRollerData.JSZT_ZCJS);
			}
			// 发送取数数据
			deviceBrakRoller.sendMessage(qjcjg);
			logger.info("发送取数据命令");
		}
		if (ml.equalsIgnoreCase(zlbs) || ml.equalsIgnoreCase(ylbs) || ml.equalsIgnoreCase(zylbs)) {
			isbs = true;
		}

		// 左边第三滚速度传感器有问题
		if (ml.equalsIgnoreCase(zcgqcw)) {
			// 当传感器有问题时，结束检测
			checkingFlage = false;
			isError = true;
			deviceBrakRoller.getDisplay().sendMessage("左第三滚筒", DeviceDisplay.SP);
			deviceBrakRoller.getDisplay().sendMessage("速度传感器异常", DeviceDisplay.XP);
		}

		if (ml.equalsIgnoreCase(ycgqcw)) {
			checkingFlage = false;
			isError = true;
			deviceBrakRoller.getDisplay().sendMessage("右第三滚筒", DeviceDisplay.SP);
			deviceBrakRoller.getDisplay().sendMessage("速度传感器异常", DeviceDisplay.XP);
		}
	}

	@Override
	public void init(DeviceBrakRoller deviceBrakRoller) {

		this.deviceBrakRoller = deviceBrakRoller;
		tempByte = new ArrayList<Byte>();
		dataTempByte = new ArrayList<Byte>();
		weighDataTempByte=new ArrayList<Byte>();
	}

}
