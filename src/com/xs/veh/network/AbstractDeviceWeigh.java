package com.xs.veh.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.driver.DeviceWeighDriverOfJXZB10;

public abstract class AbstractDeviceWeigh extends AbstractDevice {
	static Logger logger = Logger.getLogger(AbstractDeviceWeigh.class);
	protected DeviceWeigh deviceWeigh;

	protected DeviceDisplay display;

	protected DeviceSignal signal;
	
	protected BrakRollerData brakRollerData;
	
	protected Integer s1;
	
	public abstract BrakRollerData startCheck(VehFlow vehFlow) throws IOException, InterruptedException;

	public void device2pc(byte[] ed) throws IOException {
		for (byte b : ed) {
			temp.add(b);
		}
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
	
	private List<Byte> temp = new LinkedList<Byte>();

	public List<Byte> getTemp() {
		return temp;
	}
	
	public void init(DeviceWeigh deviceWeigh) {
		this.deviceWeigh = deviceWeigh;
		display=deviceWeigh.getDisplay();
		signal=deviceWeigh.getSignal();
		s1=deviceWeigh.getS1();
	}


}
