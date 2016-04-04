package com.xs.veh.network.decode;

import java.io.IOException;
import java.util.ArrayList;

import com.xs.common.CharUtil;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceSideslip;
import com.xs.veh.network.DeviceSideslipDecode;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.SideslipData;

public class DeviceSideslipDecodeOfJxch extends DeviceSideslipDecode {
	
	// 取仪表数据
	private String qs;

	// 数据检测结束
	private String sjjcjs;
	
	private boolean checkingFlag=false;
	
	private boolean isGetData=false;

	public ProtocolType getProtocolType(byte[] bs) {
		if((bs[0]==0xF0||bs[0]==0xFC)&&bs.length==4){
			return ProtocolType.DATA;
		}else if((bs[0]==0xF0||bs[0]==0xFC)&&bs.length==8){
			return ProtocolType.DATA_AND_NOTICE;
		}
		else{
			return ProtocolType.NOTICE;
		}
	}

	public void setData(byte[] bs, SideslipData sideslipData) {
		
		Integer type = CharUtil.byteToInt(bs[0]);
		Float data =Float.parseFloat(CharUtil.bcd2Str(new byte[]{bs[1],bs[2]}));
		if(type==0xFF){
			return;
		}
		if(type==0xFC){
			data=-data;
		}
		sideslipData.getDatas().add((float)(data/10.0));
	}

	public void setSideslip(byte[] bs, SideslipData sideslipData) {
		Integer type = CharUtil.byteToInt(bs[0]);
		Float data =Float.parseFloat(CharUtil.bcd2Str(new byte[]{bs[4],bs[5]}));
		if(type==0xFC){
			data=-data;
		}
		sideslipData.setSideslip((float) (data/10.0));
		
	}

	@Override
	public void startCheck() throws  IOException, InterruptedException {
		
		// 开始新的一次检测
		createNew();
		// 显示屏显示信息
		display.sendMessage("苏J00001", DeviceDisplay.SP);
		display.sendMessage("前转向轮侧滑", DeviceDisplay.XP);
		
		// 等待测量结束
		while (this.checkingFlag) {
			Thread.sleep(300);
		}
		this.display.sendMessage("苏J00001", DeviceDisplay.SP);
		this.display.sendMessage("侧滑:"+this.sideslipData.getSideslip(), DeviceDisplay.XP);
	}

	@Override
	public void device2pc(byte[] endodedData) throws IOException {
		
		if(checkingFlag){
			setData(endodedData, sideslipData);
			int l = endodedData.length;
			String ml = CharUtil.byte2HexOfString(new byte[]{endodedData[l-4],endodedData[l-3],endodedData[l-2],endodedData[l-1]});
			
			if(ml.equals(sjjcjs)){
				deviceSideslip.sendMessage(qs);
				System.out.println("发送取数命令："+qs);
				this.isGetData=true;
				return;
			}
			
			if(isGetData){
				System.out.println("结果返回:"+CharUtil.byte2HexOfString(endodedData));
				setSideslip(endodedData, sideslipData);
				this.checkingFlag=false;
				this.isGetData=false;
			}
		}
		
	}
	
	/**
	 * 创建一次新的检测数据
	 */
	private void createNew() {
		this.checkingFlag=true;
		this.sideslipData = new SideslipData();
		sideslipData.setDatas(new ArrayList<Float>());

	}

	@Override
	public void init(DeviceSideslip deviceSideslip) {
		super.init(deviceSideslip);
		qs = (String) deviceSideslip.getQtxxObject().get("g-qs");
		sjjcjs = (String) deviceSideslip.getQtxxObject().get("r-sjjcjs");
	}
	
	
	

}
