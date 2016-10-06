package com.xs.veh.network;

import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.BrakRollerData;

public abstract class AbstractDeviceWeigh {
	
	protected DeviceWeigh deviceWeigh;

	protected DeviceDisplay display;

	protected DeviceSignal signal;
	
	protected BrakRollerData brakRollerData;
	
	protected Integer s1;
	
	public abstract BrakRollerData startCheck(VehFlow vehFlow) throws Exception;

	public abstract void device2pc(byte[] data) throws Exception;
	
	public void init(DeviceWeigh deviceWeigh) {
		this.deviceWeigh = deviceWeigh;
		display=deviceWeigh.getDisplay();
		signal=deviceWeigh.getSignal();
		s1=deviceWeigh.getS1();
	}


}
