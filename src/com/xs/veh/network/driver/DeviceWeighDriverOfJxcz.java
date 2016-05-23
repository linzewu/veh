package com.xs.veh.network.driver;

import java.io.IOException;

import com.xs.common.CharUtil;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceWeigh;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceWeigh;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.WeighData;

public class DeviceWeighDriverOfJxcz extends AbstractDeviceWeigh {

	// 开始称重
	private String kscz;

	private String jscz;
	
	public ProtocolType getProtocolType(byte[] bs) {
		return ProtocolType.DATA;
	}

	public void setData(byte[] bs, WeighData weighData) {
		String t1 = CharUtil.bcd2Str(new byte[] { bs[1], bs[2] });
		String t2 = CharUtil.bcd2Str(new byte[] { bs[5], bs[6] });
		weighData.setLeftData(Integer.parseInt(t1));
		weighData.setRightData(Integer.parseInt(t2));
	}

	@Override
	public WeighData startCheck(VehFlow vehFlow) throws IOException, InterruptedException {

		String zs =vehFlow.getJyxm().substring(1,2);
		
		String hphm =vehFlow.getHphm();
		
		// 开始新的一次检测
		createNew();
		// 显示屏显示信息
		this.display.sendMessage(hphm, DeviceDisplay.SP);
		this.display.sendMessage(zs+"轴称重请到位", DeviceDisplay.XP);
		// 开始称重
		deviceWeigh.sendMessage(kscz);
		int i = 0;
		while (true) {

			if (this.signal.getSignal(s1)) {
				if (weighData.getLeftData() != null && weighData.getRightData() != null) {
					this.display.sendMessage(zs+"轴称重已到位", DeviceDisplay.SP);
					this.display.sendMessage((weighData.getLeftData() + weighData.getRightData()) + "KG",
							DeviceDisplay.XP);
				}
				i++;
			} else {
				this.display.sendMessage(vehFlow.getHphm(), DeviceDisplay.SP);
				this.display.sendMessage(zs+"轴称重请到位", DeviceDisplay.XP);
				i = 0;
			}

			if (i >= 6) {
				break;
			}

			Thread.sleep(500);
		}
		deviceWeigh.sendHead(jscz);
		
		this.display.sendMessage(zs+"轴称重结束", DeviceDisplay.SP);
		this.display.sendMessage((weighData.getLeftData() + weighData.getRightData()) + "KG", DeviceDisplay.XP);
		
		return weighData;

	}

	private void createNew() {
		this.weighData = new WeighData();
	}

	@Override
	public void device2pc(byte[] endodedData) throws IOException {
		ProtocolType type = getProtocolType(endodedData);
		// 响应数据的处理方法
		if (type == ProtocolType.DATA) {
			setData(endodedData, weighData);
		}
	}

	@Override
	public void init(DeviceWeigh deviceWeigh) {
		super.init(deviceWeigh);
		kscz =(String) deviceWeigh.getQtxxObject().get("t-kscz");
		jscz =(String) deviceWeigh.getQtxxObject().get("t-jscz");
	}
	
	

}
