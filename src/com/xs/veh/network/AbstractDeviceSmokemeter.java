package com.xs.veh.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractDeviceSmokemeter extends AbstractDevice  {
	
	protected DeviceSmokemeter deviceSmokemeter;
	private List<Byte> temp = new LinkedList<Byte>();

	public List<Byte> getTemp() {
		return temp;
	}
	
	
	public abstract void sendCommon(String common,Object... param) throws IOException;
	
	
	public void init(DeviceSmokemeter deviceSmokemeter) {
		this.deviceSmokemeter = deviceSmokemeter;
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
