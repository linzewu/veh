package com.xs.veh.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.BrakRollerData;

public abstract class AbstractDeviceBrakRoller {
	
	
	protected DeviceBrakRoller deviceBrakRoller;
	
	protected BrakRollerData brakRollerData;
	
	protected VehFlow nextVehFlow;
	
	protected boolean isError=false;
	
	protected boolean isbs=false;
	
	protected boolean checkingFlage;
	
	protected boolean isPlusLoad = false;

	public abstract BrakRollerData startCheck(VehFlow vehFlow) throws SystemException, IOException, InterruptedException;
	
	public void device2pc(byte[] ed) throws IOException {
		for (byte b : ed) {
			temp.add(b);
		}
	}
	
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
	
	public VehFlow getNextVehFlow() {
		return nextVehFlow;
	}

	public void setNextVehFlow(VehFlow nextVehFlow) {
		this.nextVehFlow = nextVehFlow;
	}

	public String getZW(Integer zw){
		
		String str="";
		
		switch (zw) {
		case 1:
			str="一轴";
			break;
		case 2:
			str="二轴";
			break;
		case 3:
			str="三轴";
			break;
		case 4:
			str="四轴";
			break;
		case 5:
			str="五轴";
			break;
		case 6:
			str="六轴";
			break;
		case 0:
			str="驻车";
			break;
		default:
			str=zw.toString();
			break;
		}
		
		return str;
	}
	
	
	public void resetCheckStatus(){
		isPlusLoad=false;
		checkingFlage = true;
		isError=false;
		isbs=false;
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
	
	private List<Byte> temp = new LinkedList<Byte>();

	public List<Byte> getTemp() {
		return temp;
	}
	
	

}
