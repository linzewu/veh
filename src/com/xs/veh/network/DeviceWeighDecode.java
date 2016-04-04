package com.xs.veh.network;

import java.io.IOException;

import com.xs.veh.network.data.WeighData;

public abstract class DeviceWeighDecode {
	
	protected DeviceWeigh deviceWeigh;

	protected DeviceDisplay display;

	protected DeviceSignal signal;
	
	protected WeighData weighData;
	
	public abstract void startCheck() throws  IOException, InterruptedException;

	public abstract void device2pc(byte[] data) throws IOException;
	
	public void init(DeviceWeigh deviceWeigh) {
		this.deviceWeigh = deviceWeigh;
		display=deviceWeigh.getDisplay();
		signal=deviceWeigh.getSignal();
	}


}
