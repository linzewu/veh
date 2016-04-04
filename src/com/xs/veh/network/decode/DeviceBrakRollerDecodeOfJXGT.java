package com.xs.veh.network.decode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.common.exception.SystemException;
import com.xs.veh.network.BrakRollerData;
import com.xs.veh.network.DeviceBrakRoller;
import com.xs.veh.network.DeviceBrakRoller.BrakRollerDataType;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.DeviceBrakRollerDecode;
import com.xs.veh.network.DeviceDisplay;

public class DeviceBrakRollerDecodeOfJXGT extends DeviceBrakRollerDecode {

	private static Logger logger = Logger.getLogger(DeviceBrakRollerDecodeOfJXGT.class);

	// 启动左电机
	private String qdzdj;
	// 启动右电机
	private String qdydj;
	// 检测制动力
	private String jczdl;
	// 检测阻滞力
	private String jczzl;
	// 制动力检测结束
	private String zdjcjs;

	// 取检测结果
	private String qjcjg;

	// 到位延时
	private String dwysqdsj;

	private String jsqss;

	private String jsqxj;

	// 左轮抱死
	private String zlbs;
	// 右轮抱死
	private String ylbs;
	// 左右轮抱死
	private String zylbs;

	// 左传感器错误
	private String zcgqcw;

	// 右传感器错误
	private String ycgqcw;

	private boolean checkingFlage = false;


	public BrakRollerDataType getDataType(byte[] data) {

		int f = CharUtil.byteToInt(data[0]);

		int e = CharUtil.byteToInt(data[data.length - 1]);

		if (f == 0xAA) {
			return BrakRollerDataType.L_DATA;
		}

		if (f == 0xBB) {
			return BrakRollerDataType.R_DATA;
		}

		if (data.length == 19 && f == 0xFF && e == 0xEE) {
			return BrakRollerDataType.RESULT_DATA;
		}
		return null;
	}

	public Integer[] getCurrentData(byte[] data) {

		Integer[] array = new Integer[data.length - 2];

		for (int i = 1; i < data.length - 1; i++) {

			array[i - 1] = CharUtil.byteToInt(data[i]);

		}

		return array;
	}

	public void setCheckedData(byte[] data, BrakRollerData brakRollerData) {

		Integer zzzl = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[4], data[5] }));

		Integer yzzl = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[6], data[7] }));

		Integer zzdl = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[8], data[9] }));

		Integer yzdl = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[10], data[11] }));

		Integer zzdlc = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[12], data[13] }));

		Integer yzdlc = Integer.parseInt(CharUtil.bcd2Str(new byte[] { data[14], data[15] }));

		String fh = CharUtil.byte2HexOfString(new byte[] { data[16] }).substring(0, 1);

		Float gcc = Float.parseFloat(CharUtil.byte2HexOfString(new byte[] { data[17] })) / 10;

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

		logger.info("左阻滞力" + CharUtil.bcd2Str(new byte[] { data[4], data[5] }));
		logger.info("右阻滞力" + CharUtil.bcd2Str(new byte[] { data[6], data[7] }));
		logger.info("左阻制动力" + CharUtil.bcd2Str(new byte[] { data[8], data[9] }));
		logger.info("右阻制动力" + CharUtil.bcd2Str(new byte[] { data[10], data[11] }));
		logger.info("左最大力差" + CharUtil.bcd2Str(new byte[] { data[12], data[13] }));
		logger.info("左右最大力差" + CharUtil.bcd2Str(new byte[] { data[14], data[15] }));
		logger.info("过程差" + CharUtil.byte2HexOfString(new byte[] { data[17] }));

	}

	public ProtocolType getProtocolType(byte[] data) {
		
		//logger.info("数据：" + CharUtil.byte2HexOfString(data));

		int f = CharUtil.byteToInt(data[0]);

		int e = CharUtil.byteToInt(data[data.length - 1]);

		if (f == 0xAA) {
			return ProtocolType.DATA;
		}

		if (f == 0xBB) {
			return ProtocolType.DATA;
		}

		if (data.length == 16 && f == 0xFF && e == 0xEE) {
			return ProtocolType.DATA;
		}

		if (data.length > 4 && f == 0xFF && CharUtil.byteToInt(data[3]) == 0xEE) {
			return ProtocolType.DATA_AND_NOTICE;
		}

		if (data.length > 4 && f == 0xFF && CharUtil.byteToInt(data[2]) == 0xEE) {
			return ProtocolType.DATA;
		}

		return ProtocolType.NOTICE;
	}

	public byte[] getNotice(byte[] bs) {

		byte[] temp = new byte[4];

		System.arraycopy(bs, 0, temp, 0, temp.length);

		return temp;
	}

	public byte[] getData(byte[] bs) {
		byte[] temp = new byte[bs.length - 4];
		System.arraycopy(bs, 4, temp, 0, temp.length);
		return temp;
	}

	public static void main(String[] age) {

		Float gcc = 63f / 10;
		System.out.println(gcc);

	}

	public void setCurrentData(byte[] data) {

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
	public void startCheck() throws SystemException, IOException, InterruptedException {

		try {
			String ksjc = (String) deviceBrakRoller.getQtxxObject().get("t-ksjc");
			String ybql = (String) deviceBrakRoller.getQtxxObject().get("t-ybql");
			String dwysqdsj = (String) deviceBrakRoller.getQtxxObject().get("s-dwysqdsj");
			// String zdljcsj = (String) this.getQtxxObject().get("s-zdljcsj");
			String qjcjg = (String) deviceBrakRoller.getQtxxObject().get("g-qjcjg");

			if (ksjc == null || ybql == null || qjcjg == null) {
				throw new SystemException("滚筒制动启动错误，通讯协议不全");
			}

			checkingFlage = true;

			// 仪表清0
			deviceBrakRoller.sendMessage(ybql);
			Thread.sleep(200);
			// 清理数据
			deviceBrakRoller.clearDate();
			brakRollerData = new BrakRollerData();

			// 发送到位延时时间
			deviceBrakRoller.sendMessage(dwysqdsj);
			Thread.sleep(200);

			deviceBrakRoller.getDisplay().sendMessage("苏J00001", DeviceDisplay.SP);
			deviceBrakRoller.getDisplay().sendMessage("一轴制动请到位", DeviceDisplay.XP);

			// 等待到位
			int i = 0;
			while (true) {
				if (deviceBrakRoller.getSignal()) {
					deviceBrakRoller.getDisplay().sendMessage("苏J00001", DeviceDisplay.SP);
					deviceBrakRoller.getDisplay().sendMessage("一轴已到位", DeviceDisplay.XP);
					i++;
				} else {
					deviceBrakRoller.getDisplay().sendMessage("苏J00001", DeviceDisplay.SP);
					deviceBrakRoller.getDisplay().sendMessage("一轴制动请到位", DeviceDisplay.XP);
					i = 0;
				}

				if (i >= 6) {
					break;
				}

				Thread.sleep(500);
			}

			// 举升下降
			deviceBrakRoller.sendMessage(jsqxj);
			Thread.sleep(4000);

			// 开始检测
			deviceBrakRoller.sendMessage(ksjc);

			// 等待检测数据返回
			while (true) {
				if (this.getBrakRollerData().getZzdl() != null && this.getBrakRollerData().getYzdl() != null) {
					break;
				}
				Thread.sleep(300);
			}
			// 保存检测数据、计算检测结果
			deviceBrakRoller.getCheckDataManager().saveBrakRoller(brakRollerData);

			checkingFlage = false;
			
			deviceBrakRoller.getDisplay().sendMessage("苏J00001", DeviceDisplay.SP);
			deviceBrakRoller.getDisplay().sendMessage("一轴制动检测结束", DeviceDisplay.XP);
			logger.info("检测结束");
		} finally {
			this.deviceBrakRoller.sendMessage(jsqss);
		}

		// FF08EE06

	}

	@Override
	public void device2pc(byte[] endodedData) throws IOException {

		if (checkingFlage) {
			ProtocolType protocolType = getProtocolType(endodedData);

			if (protocolType == ProtocolType.DATA || protocolType == ProtocolType.DATA_AND_NOTICE) {

				byte[] data = endodedData;

				if (protocolType == ProtocolType.DATA_AND_NOTICE) {
					data = getData(endodedData);
				}

				BrakRollerDataType dataType = getDataType(data);

				if (dataType == BrakRollerDataType.L_DATA) {
					setCurrentData(data);
				} else if (dataType == BrakRollerDataType.R_DATA) {
					setCurrentData(data);
				} else if (dataType == BrakRollerDataType.RESULT_DATA) {
					setCheckedData(data, brakRollerData);
				}
			}

			if (protocolType == ProtocolType.NOTICE || protocolType == ProtocolType.DATA_AND_NOTICE) {

				byte[] notice = endodedData;

				if (protocolType == ProtocolType.DATA_AND_NOTICE) {
					logger.info("通知及数据一起：" + CharUtil.byte2HexOfString(endodedData));
					notice = getNotice(endodedData);
				}
				

				String ml = CharUtil.byte2HexOfString(notice);

				logger.info("仪表返回命令：" + ml);

				if (ml.equalsIgnoreCase(qdzdj)) {
					deviceBrakRoller.getDisplay().sendMessage("检测开始", DeviceDisplay.SP);
					deviceBrakRoller.getDisplay().sendMessage("请放手刹", DeviceDisplay.XP);
					return;
				}
				if (ml.equalsIgnoreCase(jczzl)) {
					ds("开始检测阻滞力", 3, null);
					return;
				}

				if (ml.equalsIgnoreCase(jczdl)) {
					deviceBrakRoller.getDisplay().sendMessage("请踩刹车", DeviceDisplay.XP);
					ds("开始检测制动力", 5, null);
					return;
				}
				// 制动检测结束
				
				if (ml.equalsIgnoreCase(zdjcjs) || ml.equalsIgnoreCase(zlbs) || ml.equalsIgnoreCase(ylbs)
						|| ml.equalsIgnoreCase(zylbs)) {
					// 发送取数数据
					deviceBrakRoller.sendMessage(qjcjg);
					logger.info("发送取数据命令");
				}

			}
		}

	}

	// 倒数计时线程
	public void ds(final String title, final Integer ms, final String afterTitle) {
		deviceBrakRoller.getExecutor().execute(new Runnable() {
			public void run() {
				try {
					deviceBrakRoller.getDisplay().sendMessage(title, DeviceDisplay.SP);
					for (int i = ms; i >= 1; i--) {
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

	@Override
	public void init(DeviceBrakRoller deviceBrakRoller) {

		this.deviceBrakRoller = deviceBrakRoller;
		qdzdj = (String) deviceBrakRoller.getQtxxObject().get("r-qdzdj");
		qdydj = (String) deviceBrakRoller.getQtxxObject().get("r-qdydj");
		jczdl = (String) deviceBrakRoller.getQtxxObject().get("r-jczdl");
		zdjcjs = (String) deviceBrakRoller.getQtxxObject().get("r-zdjcjs");
		jczzl = (String) deviceBrakRoller.getQtxxObject().get("r-jczzl");
		qjcjg = (String) deviceBrakRoller.getQtxxObject().get("g-qjcjg");
		dwysqdsj = (String) deviceBrakRoller.getQtxxObject().get("s-dwysqdsj");

		jsqss = (String) deviceBrakRoller.getQtxxObject().get("t-jsqss");

		jsqxj = (String) deviceBrakRoller.getQtxxObject().get("t-jsqxj");
		// 检测结束 左轮抱死
		zlbs = (String) deviceBrakRoller.getQtxxObject().get("r-zdjcjs-zbs");
		// 检测结束 右轮抱死
		ylbs = (String) deviceBrakRoller.getQtxxObject().get("r-zdjcjs-ybs");

		// 检测结束 左右轮抱死
		zylbs = (String) deviceBrakRoller.getQtxxObject().get("r-zdjcjs-zybs");

		// 左边第三滚速度传感器有问题
		zcgqcw = (String) deviceBrakRoller.getQtxxObject().get("r-zsdcgq");

		// 左边第三滚速度传感器有问题
		ycgqcw = (String) deviceBrakRoller.getQtxxObject().get("r-ysdcgq");

	}

}
