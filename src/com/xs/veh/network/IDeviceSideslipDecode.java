package com.xs.veh.network;

import com.xs.veh.network.SimpleRead.ProtocolType;

public interface IDeviceSideslipDecode {
	
	public ProtocolType getProtocolType(byte[] bs);
	
	public void setData(byte[] bs,SideslipData sideslipData);

}
