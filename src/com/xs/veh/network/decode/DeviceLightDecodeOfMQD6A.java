package com.xs.veh.network.decode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceLight;
import com.xs.veh.network.DeviceLightDecode;
import com.xs.veh.network.LightData;
import com.xs.veh.network.SimpleRead.ProtocolType;

import net.sf.json.JSONObject;

public class DeviceLightDecodeOfMQD6A extends DeviceLightDecode {

	static Logger logger = Logger.getLogger(DeviceLightDecodeOfMQD6A.class);

	private boolean checkingFlag;

	private boolean getMainBeamDataFlag;

	private boolean getSideBeamDataFlag;

	private byte[] mainBeam;

	private byte[] sideBeam;

	private int temp;

	
	// 灯型
	private DX dx;

	// 光型
	private GX gx;

	public DeviceLightDecodeOfMQD6A() {
	}

	// 取当前数据
	private String byte2String(byte[] bs) {
		return new String(bs);
	}

	public LightNoticeType getLightNoticeType(byte[] bs) {
		String rtx = CharUtil.byte2HexOfString(bs);
		if ("0232544F29".equals(rtx)) {
			return LightNoticeType.highBeamOfMainEnd;
		}

		if ("0232544E2A".equals(rtx)) {
			return LightNoticeType.highBeamOfSideEnd;
		}
		if ("0232544D2B".equals(rtx)) {
			return LightNoticeType.lowBeamOfMainEnd;
		}

		return null;

	}

	public ProtocolType getProtocolType(byte[] bs) {

		if (bs.length == 5) {
			return ProtocolType.NOTICE;
		} else {
			return ProtocolType.DATA;
		}
	}

	/**
	 * 设置数据
	 * 
	 * @param bs
	 *            返回的字节数据
	 * @param highBeamOfMain
	 *            主远灯
	 * @param lowBeamOfMain
	 *            主近灯
	 * @param highBeamOfSide
	 *            副远灯
	 */
	public void setData(byte[] bs) {

		char type = (char) bs[1];
		if (bs.length == 39) {
			// 主远灯
			byte[] highData = new byte[18];
			System.arraycopy(bs, 2, highData, 0, highData.length);
			
			
			if(!isEmptyData(highData)){
				if (type == 'R'){
					highBeamOfMainRigth =new LightData();
					setCurrent(highData, highBeamOfMainRigth);
				}
				if (type == 'L'){
					highBeamOfMainLeft =new LightData();
					setCurrent(highData, highBeamOfMainLeft);
				}
			}

			// 主近灯
			byte[] lowData = new byte[18];
			System.arraycopy(bs, 20, lowData, 0, lowData.length);
			if(!isEmptyData(highData)){
				if (type == 'R'){
					lowBeamOfMainRigth =new LightData();
					setCurrent(lowData, lowBeamOfMainRigth);
				}
				
				if (type == 'L'){
					lowBeamOfMainLeft =new LightData();
					setCurrent(lowData, lowBeamOfMainLeft);
				}
			}
		}
		//副灯数据
		if (bs.length == 21) {
			byte[] highData = new byte[18];
			System.arraycopy(bs, 2, highData, 0, highData.length);
			
			if (type == 'R'){
				highBeamOfSideRigth =new LightData();
				setCurrent(highData, highBeamOfSideRigth);
			}
			if (type == 'L'){
				highBeamOfSideLeft =new LightData();
				setCurrent(highData, highBeamOfSideLeft);
			}
		}
	}

	private boolean isEmptyData(byte[] highData) {
		
		if(new String(highData).equals("000000000000000000")){
			return true;
		}
		
		return false;
	}

	private void setCurrent(byte[] bs, LightData lightData) {
		byte[] data1 = new byte[5];
		System.arraycopy(bs, 0, data1, 0, data1.length);
		lightData.setSppc(byte2String(data1));

		byte[] data2 = new byte[5];
		System.arraycopy(bs, 5, data2, 0, data2.length);
		lightData.setCzpc(byte2String(data2));

		byte[] data3 = new byte[4];
		System.arraycopy(bs, 10, data3, 0, data3.length);
		lightData.setGq(byte2String(data3));

		byte[] data4 = new byte[4];
		System.arraycopy(bs, 14, data4, 0, data4.length);
		lightData.setDg(byte2String(data4));
		
		

	}

	@Override
	public void sysSetting() throws IOException, InterruptedException {

		String qtxx = deviceLight.getDevice().getQtxx();
		// 获取开机设置信息
		JSONObject jo = JSONObject.fromObject(qtxx);
		String kwfx = (String) jo.get("s-kwfx");
		String kwgd = (String) jo.get("s-kwgd");
		String yjgqhsj = (String) jo.get("s-yjgqhsj");
		String zfdqhsj = (String) jo.get("s-zfdqhsj");
		String sjdw = (String) jo.get("s-sjdw");
		String ms = (String) jo.get("s-ms");
		String dxqhtb = (String) jo.get("s-dxqhtb");
		String bc = (String) jo.get("s-bc");
		logger.info(jo.toString());

		if (kwfx != null) {
			deviceLight.sendMessage(kwfx);
			Thread.sleep(100);
		}
		if (kwgd != null) {
			deviceLight.sendMessage(kwgd);
			Thread.sleep(100);
		}

		if (yjgqhsj != null) {
			deviceLight.sendMessage(yjgqhsj);
			Thread.sleep(100);
		}

		if (zfdqhsj != null) {
			deviceLight.sendMessage(zfdqhsj);
			Thread.sleep(100);
		}

		if (sjdw != null) {
			deviceLight.sendMessage(sjdw);
			Thread.sleep(100);
		}

		if (ms != null) {
			deviceLight.sendMessage(ms);
			Thread.sleep(100);
		}

		if (dxqhtb != null) {
			deviceLight.sendMessage(dxqhtb);
			Thread.sleep(100);
		}
		if (bc != null) {
			deviceLight.sendMessage(bc);
			Thread.sleep(100);
		}

	}

	public Map<String, String> getSetting(DeviceLight deviceLight) {
		
		return createSettingData();
	}

	private void setting(Map<String, String> setting, DeviceLight deviceLight)
			throws InterruptedException, IOException {

		Set<String> array = setting.keySet();
		for (String ml : array) {
			deviceLight.sendMessage(setting.get(ml));
			Thread.sleep(100);
		}
	}

	@Override
	public void startCheck(String clzd, String clyd) throws IOException, InterruptedException {

		Map<String, String> setting = this.getSetting(deviceLight);
		String dxml = (String) setting.get("dx");
		dx = pddx(dxml);
		String gxml = (String) setting.get("gx");
		gx = pdgx(gxml);
		setting(setting, deviceLight);

		if (clzd != null) {
			// 开始测量
			this.checkingFlag = true;
			if (gx == GX.DCYG || gx == GX.YJGJC) {
				deviceLight.getDisplay().sendMessage("开始测量左远光灯", DeviceDisplay.SP);
				deviceLight.getDisplay().sendMessage("请打开远光灯", DeviceDisplay.XP);
			}
			if (gx == GX.DCJG) {
				deviceLight.getDisplay().sendMessage("开始测量右近光灯", DeviceDisplay.SP);
				deviceLight.getDisplay().sendMessage("请打开近光灯", DeviceDisplay.XP);
			}
			// 测量左灯
			deviceLight.sendMessage(clzd);
			while (true) {
				if (!this.checkingFlag) {
					break;
				}
				Thread.sleep(100);
			}
		}

		if (clyd != null) {
			// 开始测量
			this.checkingFlag = true;
			if (gx == GX.DCYG || gx == GX.YJGJC) {
				deviceLight.getDisplay().sendMessage("开始测量右远光灯", DeviceDisplay.SP);
				deviceLight.getDisplay().sendMessage("请打开远光灯", DeviceDisplay.XP);
			}
			if (gx == GX.DCJG) {
				deviceLight.getDisplay().sendMessage("开始测量右近光灯", DeviceDisplay.SP);
				deviceLight.getDisplay().sendMessage("请打开近光灯", DeviceDisplay.XP);
			}
			// 测量右灯
			deviceLight.sendMessage(clyd);
			while (true) {
				if (!this.checkingFlag) {
					break;
				}
				Thread.sleep(100);
			}
		}
		
		String qsml = deviceLight.getQtxxObject().getString("g-qzzdsj");
		getMainBeamData(qsml);

		qsml = deviceLight.getQtxxObject().getString("g-qyzdsj");
		getMainBeamData(qsml);
		
		
		
	//	this.checkDataManager.lightSave
		
		Thread.sleep(1000);
		// 归位
		String yqgw = (String) deviceLight.getQtxxObject().get("s-yqgw");
		deviceLight.sendMessage(yqgw);
		
		logger.info("左主远光灯： "+ this.highBeamOfMainLeft);
		logger.info("右主远光灯： "+ this.highBeamOfMainRigth);
		
		logger.info("右主近光灯： "+ this.lowBeamOfMainRigth);
		logger.info("左主近光灯： "+ this.lowBeamOfMainLeft);

	}

	private void getMainBeamData(String qsml) throws IOException, InterruptedException {
		mainBeam = new byte[39];
		deviceLight.sendMessage(qsml);
		this.getMainBeamDataFlag = true;
		while (this.getMainBeamDataFlag) {
			Thread.sleep(300);
		}
	}

	@Override
	public void device2pc(byte[] endodedData) throws IOException {

		ProtocolType type = this.getProtocolType(endodedData);
		// 响应数据主灯数据
		if (this.getMainBeamDataFlag) {
			System.arraycopy(endodedData, 0, mainBeam, temp, endodedData.length);
			temp += endodedData.length;
			if (temp >= mainBeam.length) {
				System.out.println("数据：" + new String(mainBeam));
				setData(mainBeam);
				this.getMainBeamDataFlag = false;
				temp=0;
			}
		}
		
		

		// 响应通知的方法
		if (type == ProtocolType.NOTICE) {
			logger.info("仪器响应通知：" + CharUtil.byte2HexOfString(endodedData));

			// 解析响应命令
			LightNoticeType lightNoticeType = getLightNoticeType(endodedData);

			String xpString = null;
			String spString = null;
			// 远光灯测量结束
			if (lightNoticeType == LightNoticeType.highBeamOfMainEnd) {
				// 只测试远光 并且是二灯制
				if (dx == DX.EDZ && gx == GX.DCYG) {
					this.checkingFlag = false;
					return;
				}

				if (dx == DX.SDZ) {
					spString = "开始测量副远光灯";
					xpString = "请打开副远光灯";
				} else if (dx == DX.EDZ && gx == GX.YJGJC) {
					spString = "开始测量近光灯";
					xpString = "请切换近光灯";
				}
				deviceLight.getDisplay().sendMessage(spString, DeviceDisplay.SP);
				deviceLight.getDisplay().sendMessage(xpString, DeviceDisplay.XP);
				return;
			}
			if (lightNoticeType == LightNoticeType.highBeamOfSideEnd) {

				// // 只测试远光
				if (gx == GX.DCYG) {
					this.checkingFlag = false;
					return;
				}
				if (gx == GX.YJGJC) {
					spString = "开始测量近光灯";
					xpString = "请切换近光灯";
					deviceLight.getDisplay().sendMessage(spString, DeviceDisplay.SP);
					deviceLight.getDisplay().sendMessage(xpString, DeviceDisplay.XP);
					return;
				}
			}
			if (lightNoticeType == LightNoticeType.lowBeamOfMainEnd) {
				deviceLight.getDisplay().sendMessage("", DeviceDisplay.SP);
				deviceLight.getDisplay().sendMessage("", DeviceDisplay.XP);
				this.checkingFlag = false;
				return;
			}
		}

	}

	/**
	 * 判断灯型 二灯制 四灯制
	 * 
	 * @param ml
	 * @return
	 */
	private DX pddx(String ml) {
		String edz = (String) deviceLight.getQtxxObject().get("s-edz");
		String sdz = (String) deviceLight.getQtxxObject().get("s-sdz");
		if (ml.equals(edz)) {
			return DX.EDZ;
		}
		if (ml.equals(sdz)) {
			return DX.SDZ;
		}
		return null;
	}

	/**
	 * 判断光型 只测远光 只测近光 远近光均测
	 * 
	 * @param ml
	 * @return
	 */
	private GX pdgx(String ml) {
		String dcyg = (String) deviceLight.getQtxxObject().get("s-dcyg");
		String dcjg = (String) deviceLight.getQtxxObject().get("s-dcjg");
		String yjgjc = (String) deviceLight.getQtxxObject().get("s-yjgjc");
		if (ml.equals(dcyg)) {
			return GX.DCYG;
		}
		if (ml.equals(dcjg)) {
			return GX.DCJG;
		}
		if (ml.equals(yjgjc)) {
			return GX.YJGJC;
		}
		return null;
	}

	/**
	 * 判断远光灯调整
	 * 
	 * @param ml
	 */
	private YGTZ pdygdtz(String ml) {

		String yes = (String) deviceLight.getQtxxObject().get("s-xytzyg");
		String no = (String) deviceLight.getQtxxObject().get("s-bxytzyg");

		if (ml.equals(yes)) {
			return YGTZ.YG_YES;
		}
		if (ml.equals(no)) {
			return YGTZ.YG_NO;
		}
		return null;
	}

	/**
	 * 判断近光灯单独调整
	 * 
	 * @param ml
	 * @return
	 */
	private JGTZ pdjgdtz(String ml) {

		String yes = (String) deviceLight.getQtxxObject().get("s-xytzjg");
		String no = (String) deviceLight.getQtxxObject().get("s-bxytzjg");

		if (ml.equals(yes)) {
			return JGTZ.JG_YES;
		}
		if (ml.equals(no)) {
			return JGTZ.JG_NO;
		}

		return null;
	}

	@Override
	public void setDeviceLight(DeviceLight deviceLight) {

		this.deviceLight = deviceLight;

	}

	@Override
	public Map<String, String> createSettingData() {
		String qtxx = deviceLight.getDevice().getQtxx();
		JSONObject jo = JSONObject.fromObject(qtxx);
		String edz = (String) jo.get("s-edz");
		String yjgjc = (String) jo.get("s-yjgjc");
		String bxytzyg = (String) jo.get("s-bxytzyg");
		String bxytzjg = (String) jo.get("s-bxytzjg");
		Map<String, String> data = new HashMap<String, String>();

		data.put("dx", edz);
		data.put("gx", yjgjc);
		data.put("ygtz", bxytzyg);
		data.put("jgtz", bxytzjg);
		return data;
	}

	public static void main(String[] age) {

		LightData l =null;
		
		LightData d=l;
		
		d=new LightData();
		
		System.out.println(l);
		
		

	}

}
