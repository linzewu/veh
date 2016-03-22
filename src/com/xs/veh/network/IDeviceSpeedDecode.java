package com.xs.veh.network;

import com.xs.veh.network.SimpleRead.ProtocolType;

public interface IDeviceSpeedDecode {
	
	public ProtocolType getProtocolType(byte[] bs);
	
	public void setData(byte[] bs,SpeedData speedData);

}
