package com.xs.veh.network;

import java.io.IOException;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.SpeedData;

public abstract class AbstractDeviceSpeed {
	
	protected DeviceSpeed deviceSpeed;

	protected DeviceDisplay display;

	protected DeviceSignal signal;
	
	protected SpeedData speedData;
	
	protected Integer s1;
	
	public abstract SpeedData startCheck(VehCheckLogin vehCheckLogin,VehFlow vehFlow) throws  IOException, InterruptedException;

	public abstract void device2pc(byte[] data) throws IOException;
	
	public void init(DeviceSpeed deviceSpeed) {
		this.deviceSpeed = deviceSpeed;
		display=deviceSpeed.getDisplay();
		signal=deviceSpeed.getSignal();
		s1=deviceSpeed.getS1();
	}


}
