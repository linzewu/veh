package com.xs.veh.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.DynoData;
public abstract class AbstractDeviceDyno extends  AbstractDevice{
	
	protected DeviceDyno deviceDyno;
	protected DeviceDisplay display;
	protected DynoData dynoData;
	
	protected DeviceSignal signal;
	protected Integer s1;
	
	private List<Byte> temp = new LinkedList<Byte>();
	

	public List<Byte> getTemp() {
		return temp;
	}
	
	public void device2pc(byte[] ed) throws IOException {
		for (byte b : ed) {
			temp.add(b);
		}
	}
	
	public abstract void sendCommon(String common,Object... param) throws IOException, InterruptedException;
	
	
	public void init(DeviceDyno deviceDyno) {
		this.deviceDyno = deviceDyno;
		display=deviceDyno.getDisplay();
		this.signal=deviceDyno.getSignal();
		this.s1=deviceDyno.getS1();
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
	
	public void tempiSEmpty() throws InterruptedException {
		int count=0;
		while (temp.isEmpty()) {
			Thread.sleep(50);
			count++;
			if(count==3) {
				throw new InterruptedException("数据等待超时！");
			}
		}
	}
	
	public byte[] getDevData(byte[] contex, byte beginByte) throws InterruptedException {
		
		try {
			tempiSEmpty();
			while (temp.remove(0)!=beginByte) {
				tempiSEmpty();
			}
		}catch (Exception e) {
			return null;
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
