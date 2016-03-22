package com.xs.veh.network.decode;

import com.xs.common.CharUtil;
import com.xs.veh.network.IDeviceSpeedDecode;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.SpeedData;

public class DeviceSpeedDecodeOfJxsd implements IDeviceSpeedDecode {

	@Override
	public ProtocolType getProtocolType(byte[] bs) {
		if(bs.length==3&&bs[0]==0xFF){
			return ProtocolType.NOTICE;
		}else{
			return ProtocolType.DATA;
		}
	}
	
	
	public void setData(byte[] bs,SpeedData speedData){
		
		byte[] temp =new byte[]{bs[1],bs[2]};
		
		String speed = CharUtil.bcd2Str(temp);
		
		speedData.setSpeed(Float.parseFloat(speed));
		
	}

}
