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
	
	
	public static final byte  A= CharUtil.hexStringToByte("41")[0];
	

	@Override
	public CurbWeightData startCheck(VehCheckLogin vehCheckLogin) throws IOException, InterruptedException{
		
		deviceManyWeigh.sendMessage(ql);
		logger.info("清零返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));

		String hphm = vehCheckLogin.getHphm();

		// 开始新的一次检测
		createNew();
		
		Integer qz = check(vehCheckLogin, "前");
		
		
		while (true) {
			deviceManyWeigh.sendMessage(dqgd);
			
			byte[] singData = this.getDevData(new byte[12],A);
			
			String s1str=new String(new byte[] {singData[3]});
			String s2str=new String(new byte[] {singData[4]});
			
			boolean s1 = "X".equals(s1str)?false:true;
			boolean s2 = "X".equals(s2str)?false:true;
			
			if(s1) {
				break;
			}
			Thread.sleep(500);
		}
		
		Integer hz = check(vehCheckLogin, "后");
		
		CurbWeightData curbWeightData=new CurbWeightData();
		
		curbWeightData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), "Z1");
		
		curbWeightData.setQzzl(qz);
		
		curbWeightData.setHzzl(hz);
		
		curbWeightData.setZbzl(qz+hz);
		

		return curbWeightData;

	}
	
	
	private Integer check(VehCheckLogin vehCheckLogin,String zw) throws IOException, InterruptedException {
		// 显示屏显示信息
		this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
		this.display.sendMessage(zw+"轴称重请到位", DeviceDisplay.XP);
		int i = 0;
		while (true) {
			deviceManyWeigh.sendMessage(dqgd);
			
			byte[] singData = this.getDevData(new byte[12],A);
			
			String s1str=new String(new byte[] {singData[3]});
			String s2str=new String(new byte[] {singData[4]});
			
			boolean s1 = "X".equals(s1str)?false:true;
			boolean s2 = "X".equals(s2str)?false:true;
			
			deviceManyWeigh.sendMessage(dqsj);
			byte[] data = this.getDevData(new byte[19],A);
			logger.info("称重开始："+CharUtil.byte2HexOfString(data));
			
			Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
			Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
			
			if (!s1&&!s2&&(zlh+ylh)>100) {
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

		this.display.sendMessage(zw+"轴称重结束", DeviceDisplay.SP);
		this.display.sendMessage((zlh + ylh) + "KG", DeviceDisplay.XP);
		
		return (zlh + ylh);
	}

	private void createNew() {
		
	}

	
	@Override
	public void init(DeviceManyWeigh deviceManyWeigh) {
		super.init(deviceManyWeigh);
	}

}
