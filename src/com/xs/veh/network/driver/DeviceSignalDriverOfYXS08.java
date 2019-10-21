package com.xs.veh.network.driver;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.network.AbstractDeviceSignal;
import com.xs.veh.network.DeviceSignal;

public class DeviceSignalDriverOfYXS08 extends AbstractDeviceSignal {
	
	private Logger logger = Logger.getLogger(DeviceSignalDriverOfYXS08.class);

	public void decode(byte[] data) {
		List<Byte> signals = this.getSignals();
		//logger.info("模块返回："+CharUtil.byte2HexOfString(data));
		for (byte d : data) {
			signals.add(d);
			if (signals.size() >= signalsSize) {
				StringBuffer b = new StringBuffer();
				for (int i = 4; i < signalsSize-1; i++) {
					if (signals.get(i) == 0x02) {
						b.insert(0, "1");
					} else {
						b.insert(0, "0");
					}
				}
				this.getDeviceSignal().setRtx(b.toString());
				signals.clear();
				return;
			}
		}
	}

	@Override
	public void init(DeviceSignal deviceSignal) {
		this.setDeviceSignal(deviceSignal);
		signalsSize = 13;
		this.setSignals(new ArrayList<Byte>());
	}

}
