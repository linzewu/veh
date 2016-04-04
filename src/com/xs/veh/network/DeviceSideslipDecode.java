package com.xs.veh.network;

import java.io.IOException;

import com.xs.veh.network.data.SideslipData;

public abstract class DeviceSideslipDecode {

	protected DeviceSideslip deviceSideslip;
	
	protected DeviceDisplay display;
	
	protected DeviceSignal signal;
	
	protected SideslipData sideslipData;

	public abstract void startCheck() throws  IOException, InterruptedException;

	public abstract void device2pc(byte[] data) throws IOException;

	public void init(DeviceSideslip deviceSideslip) {
		this.deviceSideslip = deviceSideslip;
		display=deviceSideslip.getDisplay();
		signal=deviceSideslip.getSignal();
	}


}
