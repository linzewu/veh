package com.xs.veh.network.decode;

import java.io.IOException;

import com.xs.common.CharUtil;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceWeigh;
import com.xs.veh.network.DeviceWeighDecode;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.WeighData;

public class DeviceWeighDecodeOfJxcz extends DeviceWeighDecode {

	// 开始称重
	private String kscz;

	private String jscz;
	
	private Integer s1;

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
	public void startCheck() throws IOException, InterruptedException {

		// 开始新的一次检测
		createNew();
		// 显示屏显示信息
		this.display.sendMessage("苏J00001", DeviceDisplay.SP);
		this.display.sendMessage("前轴称重请到位", DeviceDisplay.XP);

		// 开始称重
		deviceWeigh.sendMessage(kscz);
		int i = 0;
		while (true) {

			if (this.signal.getSignal(s1)) {
				if (weighData.getLeftData() != null && weighData.getRightData() != null) {
					this.display.sendMessage("前轴称重已到位", DeviceDisplay.SP);
					this.display.sendMessage((weighData.getLeftData() + weighData.getRightData()) + "KG",
							DeviceDisplay.XP);
				}
				i++;
			} else {
				this.display.sendMessage("苏J00001", DeviceDisplay.SP);
				this.display.sendMessage("前轴称重请到位", DeviceDisplay.XP);
				i = 0;
			}

			if (i >= 6) {
				break;
			}

			Thread.sleep(500);
		}
		deviceWeigh.sendHead(jscz);
		
		this.display.sendMessage("前轴称重结束", DeviceDisplay.SP);
		this.display.sendMessage((weighData.getLeftData() + weighData.getRightData()) + "KG", DeviceDisplay.XP);

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
		s1=deviceWeigh.getQtxxObject().getInt("kzsb-xhw");
		kscz =(String) deviceWeigh.getQtxxObject().get("t-kscz");
		jscz =(String) deviceWeigh.getQtxxObject().get("t-jscz");
	}
	
	

}
