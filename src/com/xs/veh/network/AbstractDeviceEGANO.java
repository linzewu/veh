package com.xs.veh.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.DynoData;
import com.xs.veh.network.data.HBNOData;

/**
 * 尾气分析器NO
 * @author linze
 *
 */
public abstract class AbstractDeviceEGANO extends AbstractDevice {
	
	protected DeviceEGANO deviceEGANO;
	protected HBNOData data;
	private List<Byte> temp = new LinkedList<Byte>();

	public List<Byte> getTemp() {
		return temp;
	}
	
	public abstract void sendCommon(String common,Object... param) throws IOException;
	
	
	public void init(DeviceEGANO deviceEGANO) {
		this.deviceEGANO = deviceEGANO;
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
