package com.xs.veh.network.driver;

import java.io.IOException;

import com.xs.common.CharUtil;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceSpeed;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceSpeed;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.SpeedData;

public class DeviceSpeedDriverOfJxsd extends AbstractDeviceSpeed {

	// private String up;

	private String down;

	private String end;

	private String qs;

	private boolean checkingFlag;

	public ProtocolType getProtocolType(byte[] bs) {
		if (bs.length == 3 && CharUtil.byteToInt(bs[0]) == 0xFF) {
			return ProtocolType.NOTICE;
		} else {
			return ProtocolType.DATA;
		}
	}

	public void setData(byte[] bs, SpeedData speedData) {

		byte[] temp = new byte[] { bs[1], bs[2] };

		String speed = CharUtil.bcd2Str(temp);

		speedData.setSpeed(Float.parseFloat(speed));

	}

	@Override
	public SpeedData startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow) throws IOException, InterruptedException {
		// 开始新的一次检测
		createNew();
		// 显示屏显示信息
		this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
		this.display.sendMessage("速度上线检测", DeviceDisplay.XP);
		int i = 0;
		while (true) {
			if (this.signal.getSignal(s1)) {
				this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
				this.display.sendMessage("速度到位", DeviceDisplay.XP);
				i++;
			} else {
				this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
				this.display.sendMessage("速度上线检测", DeviceDisplay.XP);
				i = 0;
			}
			if (i >= 6) {
				break;
			}
			Thread.sleep(500);
		}

		// 速度太下降
		deviceSpeed.sendMessage(down);

		Thread.sleep(4000);

		this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
		this.display.sendMessage("40KM/H 申报", DeviceDisplay.XP);

		// 等待测量结束
		while (this.speedData.getSpeed() == null) {
			Thread.sleep(500);
		}
		return speedData;
		

	}

	private void createNew() {
		this.checkingFlag = true;
		this.speedData = new SpeedData();
	}

	@Override
	public void device2pc(byte[] endodedData) throws IOException {
		System.out.println("数据：" + CharUtil.byte2HexOfString(endodedData));

		ProtocolType type = getProtocolType(endodedData);
		// 响应数据的处理方法
		if (type == ProtocolType.DATA && !this.checkingFlag) {
			setData(endodedData, speedData);

		}

		// 响应通知的方法
		if (type == ProtocolType.NOTICE) {
			String ml = CharUtil.byte2HexOfString(endodedData);
			System.out.println("命令：" + ml);
			if (ml.equals(end)) {
				deviceSpeed.sendMessage(qs);
				this.checkingFlag = false;
			}
		}
	}

	@Override
	public void init(DeviceSpeed deviceSpeed) {
		super.init(deviceSpeed);
		s1 = deviceSpeed.getQtxxObject().getInt("kzsb-xhw");
		// up = (String) deviceSpeed.getQtxxObject().get("t-up");
		down = (String) deviceSpeed.getQtxxObject().get("t-down");
		end = (String) deviceSpeed.getQtxxObject().get("r-end");
		qs = (String) deviceSpeed.getQtxxObject().get("g-qs");

	}

}
