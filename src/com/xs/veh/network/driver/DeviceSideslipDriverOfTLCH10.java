package com.xs.veh.network.driver;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceSideslip;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceSideslip;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.TakePicture;
import com.xs.veh.network.data.SideslipData;

public class DeviceSideslipDriverOfTLCH10 extends AbstractDeviceSideslip {
	
	private Logger logger = Logger.getLogger(DeviceSideslipDriverOfTLCH10.class);
	
	private String ql;
	
	private String ksjc;
	
	private String fw;
	public static final byte  A= CharUtil.hexStringToByte("41")[0];

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
	
	public static void main(String[] age) {
		DecimalFormat decimalFormat = new DecimalFormat("0.0");
		System.out.println(String.format("%.1f", 1.66));
	}

	@Override
	public SideslipData startCheck(VehFlow vehFlow) throws  IOException, InterruptedException {
		// 开始新的一次检测
		createNew();
		logger.info("发送侧滑清零:"+ql);
		deviceSideslip.sendMessage(ql);
		logger.info("侧滑清零:"+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
		Thread.sleep(200);
		// 显示屏显示信息
		logger.info("发送开始检测:");
		deviceSideslip.sendMessage(ksjc);
		logger.info("开始检测："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
		logger.info("开始检测："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
		 
		
		String hphm = vehFlow.getHphm();
		display.sendMessage(hphm, DeviceDisplay.SP);
		display.sendMessage("前转向轮侧滑时速5-10KM通过", DeviceDisplay.XP);
		
		try {
			// 等待测量结束
			while (true) {
				byte[] data = this.getDevData(new byte[10]);
				String strData=new String(new byte[]{data[3],data[4],data[5],data[6],data[7],data[8]}).trim();
				logger.info("侧滑："+strData);
				float  sideslip=Float.parseFloat(strData);
				String strSideslip = String.format("%.1f", sideslip);
				sideslip=Float.parseFloat(strSideslip);
				
				if(data[2]==0x7a){
					TakePicture.createNew(this.deviceSideslip.getVehCheckLogin(),"A1");
					this.sideslipData.setSideslip(sideslip);
					
					this.display.sendMessage(hphm, DeviceDisplay.SP);
					this.display.sendMessage("侧滑:"+this.sideslipData.getSideslip(), DeviceDisplay.XP);
					return sideslipData;
				}else{
					this.sideslipData.getDatas().add(sideslip);
					if(this.sideslipData.getDatas().size()>=1000){
						this.sideslipData.getDatas().remove(0);
					}
				}
			}
		}catch (Exception e) {
			//仪表异常
			logger.info("仪表异常返回，强制复位："+fw);
			// 显示屏显示信息
			deviceSideslip.sendMessage(fw);
			display.sendMessage("异常返回,复位...!", DeviceDisplay.XP);
			Thread.sleep(5000);
			//deviceSideslip.sendMessage(ql);
			throw e;
		}
	}
	/**
	 * 创建一次新的检测数据
	 */
	private void createNew() {
		this.sideslipData = new SideslipData();
		sideslipData.setDatas(new ArrayList<Float>());
		this.getTemp().clear();
	}

	@Override
	public void init(DeviceSideslip deviceSideslip) {
		super.init(deviceSideslip);
		ql="41046259";
		ksjc="41046655";
		fw="41045269";
	}
	
	
	

}
