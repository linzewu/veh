package com.xs.veh.network.decode;

import com.xs.veh.network.IDeviceSignalDecode;

public class DeviceSignalDecodeOfEDA9061 implements IDeviceSignalDecode {

	public String decode(byte[] data) {
		Integer s = Integer.parseInt(new String(data).substring(3,5), 16);
		
		StringBuffer b =new StringBuffer(Integer.toBinaryString(s));
		   while(b.length()<8){  
		        b.insert(0, '0');  
		    }
		return b.toString();
	}
}
