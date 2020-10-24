package com.xs.veh.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.data.CurbWeightData;

public abstract class AbstractDeviceManyWeigh extends AbstractDevice {
	static Logger logger = Logger.getLogger(AbstractDeviceManyWeigh.class);
	
	protected DeviceManyWeigh deviceManyWeigh;

	protected DeviceDisplay display;
	
	protected boolean qzdw=false;
	protected boolean hzdw=false;
	
	protected boolean zcdw=false;
	
	public abstract CurbWeightData startCheck(VehCheckLogin vc) throws Exception, InterruptedException;
	
	public abstract Integer startCheckQdz(VehCheckLogin vc) throws Exception, InterruptedException;

	public void device2pc(byte[] ed) throws IOException {
		for (byte b : ed) {
			temp.add(b);
		}
	}
	
	public byte[] getDevData(byte[] contex) throws InterruptedException, TimeoutException {
		
		long timeout=2000;
		long currentTime=0;
		
		for (int i = 0; i < contex.length; i++) {
			while (temp.isEmpty()) {
				Thread.sleep(50);
				currentTime=currentTime+50;
				if(currentTime>timeout) {
					throw new TimeoutException("读取数据超时！2s");
				}
			}
			contex[i] = temp.remove(0);
		}

		return contex;
	}
	
	public byte[] getDevData(byte[] contex, byte beginByte) throws InterruptedException, TimeoutException {
		
		long timeout=2000;
		long currentTime=0;
		
		while (temp.isEmpty()) {
			Thread.sleep(50);
			currentTime=currentTime+50;
			if(currentTime>timeout) {
				throw new TimeoutException("读取数据超时！2s");
			}
		}
		while (temp.isEmpty()||temp.remove(0)!=beginByte) {
			Thread.sleep(10);
			currentTime=currentTime+10;
			if(currentTime>timeout) {
				throw new TimeoutException("读取数据超时！2s");
			}
		}
		contex[0]=beginByte;
		for (int i = 1; i < contex.length; i++) {
			while (temp.isEmpty()) {
				Thread.sleep(50);
				currentTime=currentTime+50;
				if(currentTime>timeout) {
					throw new TimeoutException("读取数据超时！2s");
				}
			}
			contex[i] = temp.remove(0);
		}
		return contex;
	}
	
	private List<Byte> temp = new LinkedList<Byte>();

	public List<Byte> getTemp() {
		return temp;
	}
	
	public void init(DeviceManyWeigh deviceManyWeigh) {
		this.deviceManyWeigh = deviceManyWeigh;
		display=deviceManyWeigh.getDisplay();
	}


}
