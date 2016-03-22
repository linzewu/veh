package com.xs.veh.network.decode;

import com.xs.common.CharUtil;
import com.xs.veh.network.IDeviceSideslipDecode;
import com.xs.veh.network.SideslipData;
import com.xs.veh.network.SimpleRead.ProtocolType;

public class DeviceSideslipDecodeOfJxch implements IDeviceSideslipDecode {

	@Override
	public ProtocolType getProtocolType(byte[] bs) {
		
		System.out.println("协议判断"+CharUtil.byte2HexOfString(bs));
		
		if(bs[0]==0xF0||bs[0]==0xFC){
			return ProtocolType.DATA;
		}else{
			return ProtocolType.NOTICE;
		}
	}

	@Override
	public void setData(byte[] bs, SideslipData sideslipData) {
		
		String type = CharUtil.byte2HexOfString(new byte[]{bs[0]}).substring(1);;
		
		System.out.println(CharUtil.bcd2Str(new byte[]{bs[1]}));
		
	}

}
