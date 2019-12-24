package com.xs.veh.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.SideslipData;
import com.xs.veh.network.driver.DeviceSideslipDriverOfTLCH10;

public abstract class AbstractDeviceSideslip {
	private Logger logger = Logger.getLogger(AbstractDeviceSideslip.class);
	protected DeviceSideslip deviceSideslip;
	
	protected DeviceDisplay display;
	
	protected DeviceSignal signal;
	
	protected SideslipData sideslipData;
	
	private List<Byte> temp = new LinkedList<Byte>();

	public abstract SideslipData startCheck(VehFlow vehFlow,Integer zs) throws IOException, InterruptedException;

	public void device2pc(byte[] ed) throws IOException {
		for (byte b : ed) {
			temp.add(b);
		}
	}

	public void init(DeviceSideslip deviceSideslip) {
		this.deviceSideslip = deviceSideslip;
		display=deviceSideslip.getDisplay();
		signal=deviceSideslip.getSignal();
	}
	
	public List<Byte> getTemp() {
		return temp;
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
		StringBuffer ss = new StringBuffer("");
		for(int i=0;i<contex.length;i++) {
			ss.append(contex[i]+"  ");
		}
		logger.info("getDevData() :"+ss);

		return contex;
	}



}
