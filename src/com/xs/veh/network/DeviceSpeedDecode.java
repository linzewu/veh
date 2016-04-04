package com.xs.veh.network;

import java.io.IOException;

import com.xs.veh.network.data.SpeedData;

public abstract class DeviceSpeedDecode {
	
	protected DeviceSpeed deviceSpeed;

	protected DeviceDisplay display;

	protected DeviceSignal signal;
	
	protected SpeedData speedData;
	
	public abstract void startCheck() throws  IOException, InterruptedException;

	public abstract void device2pc(byte[] data) throws IOException;
	
	public void init(DeviceSpeed deviceSpeed) {
		this.deviceSpeed = deviceSpeed;
		display=deviceSpeed.getDisplay();
		signal=deviceSpeed.getSignal();
	}


}
