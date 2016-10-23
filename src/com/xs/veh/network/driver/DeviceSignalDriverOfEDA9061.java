package com.xs.veh.network.driver;

import java.util.ArrayList;

import com.xs.veh.network.AbstractDeviceSignal;
import com.xs.veh.network.DeviceSignal;

public class DeviceSignalDriverOfEDA9061 extends AbstractDeviceSignal {

	public void decode(byte[] data) {
		Integer s = Integer.parseInt(new String(data).substring(3, 5), 16);
		StringBuffer b = new StringBuffer(Integer.toBinaryString(s));
		while (b.length() < 8) {
			b.insert(0, '0');
		}
		//return b.toString();
	}
	
	@Override
	public void init(DeviceSignal deviceSignal) {
		this.setDeviceSignal(deviceSignal);
		signalsSize = 13;
		this.setSignals(new ArrayList<Byte>());
	}
}
