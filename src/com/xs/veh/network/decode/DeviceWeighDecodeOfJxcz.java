package com.xs.veh.network.decode;

import com.xs.common.CharUtil;
import com.xs.veh.network.IDeviceWeighDecode;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.WeighData;

public class DeviceWeighDecodeOfJxcz implements IDeviceWeighDecode {

	@Override
	public ProtocolType getProtocolType(byte[] bs) {
		
		return ProtocolType.DATA;
	}

	@Override
	public void setData(byte[] bs, WeighData weighData) {
		
		String t1=CharUtil.bcd2Str(new byte[]{bs[1],bs[2]});
		
		String t2=CharUtil.bcd2Str(new byte[]{bs[5],bs[6]});
		
		weighData.setLeftData(Integer.parseInt(t1));
		weighData.setRightData(Integer.parseInt(t2));
		
		
	}

}
