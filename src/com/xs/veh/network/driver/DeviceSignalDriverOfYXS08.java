package com.xs.veh.network.driver;

import com.xs.veh.network.AbstractDeviceSignal;

public class DeviceSignalDriverOfYXS08 extends AbstractDeviceSignal {

	public String decode(byte[] data) {
		StringBuffer b =new StringBuffer();
		for(int i=4;i<data.length-1;i++){
			if(data[i]==0x02){
				b.insert(0,"1");
			}else{
				b.insert(0,"0");
			}
		}
		return b.toString();
	}
}
