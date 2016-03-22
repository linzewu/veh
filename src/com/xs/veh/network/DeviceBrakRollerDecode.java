package com.xs.veh.network;

import java.io.IOException;

import com.xs.common.exception.SystemException;

public abstract class DeviceBrakRollerDecode {
	
	
	protected DeviceBrakRoller deviceBrakRoller;
	
	protected BrakRollerData brakRollerData;

	public abstract void startCheck() throws SystemException, IOException, InterruptedException;
	
	public abstract void device2pc(byte[] data) throws IOException;
	
	public abstract void init(DeviceBrakRoller deviceBrakRoller);

	public DeviceBrakRoller getDeviceBrakRoller() {
		return deviceBrakRoller;
	}

	public void setDeviceBrakRoller(DeviceBrakRoller deviceBrakRoller) {
		this.deviceBrakRoller = deviceBrakRoller;
	}

	public BrakRollerData getBrakRollerData() {
		return brakRollerData;
	}

	public void setBrakRollerData(BrakRollerData brakRollerData) {
		this.brakRollerData = brakRollerData;
	}
	
	
	
	

}
