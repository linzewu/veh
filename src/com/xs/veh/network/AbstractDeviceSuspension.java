package com.xs.veh.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.SuspensionData;

public abstract class AbstractDeviceSuspension extends  AbstractDevice{
	protected DeviceSuspension deviceSuspension;
	protected DeviceDisplay display;
	protected SuspensionData suspensionData;
	
	protected DeviceSignal signal;
	
	protected Integer s1;
	
	private List<Byte> temp = new LinkedList<Byte>();
	public abstract List<SuspensionData> startCheck(VehCheckLogin vehCheckLogin,VehFlow vehFlow) throws IOException, InterruptedException;

	public List<Byte> getTemp() {
		return temp;
	}
	
	public void device2pc(byte[] ed) throws IOException {
		for (byte b : ed) {
			temp.add(b);
		}
	}
	
	public void init(DeviceSuspension deviceSuspension) {
		this.deviceSuspension = deviceSuspension;
		display=deviceSuspension.getDisplay();
		this.signal=deviceSuspension.getSignal();
		this.s1=deviceSuspension.getS1();
	}
	
	public byte[] getDevData(byte[] contex) throws InterruptedException {

		for (int i = 0; i < contex.length; i++) {
			while (temp.isEmpty()) {
				Thread.sleep(50);
			}
			contex[i] = temp.remove(0);
		}

		return contex;
	}
	
	public byte[] getDevData(byte[] contex, byte beginByte) throws InterruptedException {
		while (temp.isEmpty()) {
			Thread.sleep(50);
		}
		while (temp.remove(0)!=beginByte) {
			
		}
		
		contex[0]=beginByte;
		for (int i = 1; i < contex.length; i++) {
			while (temp.isEmpty()) {
				Thread.sleep(50);
			}
			contex[i] = temp.remove(0);
		}

		return contex;
	}

}
