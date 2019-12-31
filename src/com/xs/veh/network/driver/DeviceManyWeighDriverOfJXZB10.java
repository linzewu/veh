package com.xs.veh.network.driver;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceManyWeigh;
import com.xs.veh.network.AbstractDeviceWeigh;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceManyWeigh;
import com.xs.veh.network.DeviceWeigh;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.CurbWeightData;

public class DeviceManyWeighDriverOfJXZB10 extends AbstractDeviceManyWeigh {
	
	static Logger logger = Logger.getLogger(DeviceManyWeighDriverOfJXZB10.class);

	// 开始称重
	private String dqsj="41046853";

	private String ql="41046259";
	
	private String sdcz="41046754";
	
	private String dqgd = "4104615A";
	
	private String ljml = "41046A51";
	
	private String dqlj = "41046B50";
	
	private String jcsd="4104704B";
	
	
	public static final byte  A= CharUtil.hexStringToByte("41")[0];
	

	@Override
	public CurbWeightData startCheck(VehCheckLogin vehCheckLogin) throws Exception, InterruptedException{
		
		deviceManyWeigh.sendMessage(ql);
		logger.info("清零返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));

		String hphm = vehCheckLogin.getHphm();

		// 开始新的一次检测
		createNew();
		
		Integer qz = check(vehCheckLogin, "前");
		
		logger.info("前轴检测完成");
		Thread.sleep(5000);
		this.display.sendMessage("请向前行驶", DeviceDisplay.XP);
		Thread.sleep(2000);
		
		Integer hz = check(vehCheckLogin, "后"); 
		
		CurbWeightData curbWeightData=new CurbWeightData();
		
		curbWeightData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), "Z1");
		
		curbWeightData.setQzzl(qz);
		
		curbWeightData.setHzzl(hz);
		
		curbWeightData.setZbzl(qz+hz);
		

		return curbWeightData;

	}
	
	
	private Integer check(VehCheckLogin vehCheckLogin,String zw) throws Exception, InterruptedException {
		try {
			//解出锁定
			deviceManyWeigh.sendMessage(jcsd);
			logger.info("解出锁定："+CharUtil.byte2HexOfString(this.getDevData(new byte[4],A)));
			
			// 显示屏显示信息
			this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
			this.display.sendMessage(zw+"轴称重请到位", DeviceDisplay.XP);
			int i = 0;
			while (true) {
//				deviceManyWeigh.sendMessage(dqgd);
//				byte[] singData = this.getDevData(new byte[12],A);
				
				
				deviceManyWeigh.sendMessage(dqsj);
				byte[] data = this.getDevData(new byte[19],A);
				logger.info("称重开始："+CharUtil.byte2HexOfString(data));
				
				Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
				Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
				
				boolean dw =false;
				
				if("前".equals(zw)) {
					dw=qzdw;
				}
				
				if("后".equals(zw)) {
					dw=hzdw;
				}
				
				if (dw) {
					this.display.sendMessage( zw+"轴称重已到位", DeviceDisplay.SP);
					this.display.sendMessage(zlh+"KG/"+ylh + "KG",
							DeviceDisplay.XP);
					i++;
				} else {
					this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
					this.display.sendMessage(zw+"轴称重请到位", DeviceDisplay.XP);
					i = 0;
				}

				if (i >= 12) {
					break;
				}

				Thread.sleep(500);
			}
			
			deviceManyWeigh.sendMessage(sdcz);
			logger.info("称重结果锁定："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
			deviceManyWeigh.sendMessage(dqsj);
			byte[] data = this.getDevData(new byte[19],A);
			
			deviceManyWeigh.sendMessage(ljml);
			logger.info("称重结果累加："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
			
			Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
			Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
			
			logger.info(zw+"轴称重结束"+(zlh + ylh));
			this.display.sendMessage(zw+"轴称重结束", DeviceDisplay.SP);
			this.display.sendMessage((zlh + ylh) + "KG", DeviceDisplay.XP);
			
			return (zlh + ylh);
		}catch (Exception e) {
			logger.error("整备质量检测异常",e);
			throw e;
		}
	
	}

	private void createNew() {
		qzdw=false;
		hzdw=false;
		
	}

	
	@Override
	public void init(DeviceManyWeigh deviceManyWeigh) {
		super.init(deviceManyWeigh);
	}

}
