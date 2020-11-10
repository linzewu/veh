package com.xs.veh.network.driver;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.entity.BaseEntity;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.AbstractDeviceManyWeigh;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceManyWeigh;
import com.xs.veh.network.TakePicture;
import com.xs.veh.network.data.BaseDeviceData;
import com.xs.veh.network.data.CurbWeightData;

import net.sf.json.JSONObject;

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
		
		//deviceManyWeigh.sendMessage(ql);
		//logger.info("清零返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));

		String hphm = vehCheckLogin.getHphm();

		// 开始新的一次检测
		createNew();
		
		Integer qz = check(vehCheckLogin, "前");
		
		Integer hz =0;
		if(!zcdw) {
			logger.info("前轴检测完成");
			Thread.sleep(5000);
			this.display.sendMessage("请向前行驶", DeviceDisplay.XP);
			Thread.sleep(2000);
			hz = check(vehCheckLogin, "后"); 
		}
		
		CurbWeightData curbWeightData=new CurbWeightData();
		
		curbWeightData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), "Z1");
		
		curbWeightData.setQzzl(qz);
		
		curbWeightData.setHzzl(hz);
		
		if(zcdw) {
			curbWeightData.setZbzl((qz+hz));
		}else {
			curbWeightData.setZbzl((qz+hz)-65);
		}
		
		
		
		String cllx=vehCheckLogin.getCllx();
		int xzgj=100;
		String temp1="±3%或±";
		Float temp2=0.03f;
		
		if(cllx.indexOf("H1")==0||cllx.indexOf("H2")==0||cllx.indexOf("Z1")==0||cllx.indexOf("Z2")==0||cllx.indexOf("Z5")==0||cllx.indexOf("G")==0||cllx.indexOf("B")==0){
			xzgj=500;
		}else if(cllx.indexOf("H3")==0||cllx.indexOf("H4")==0||cllx.indexOf("Z3")==0||cllx.indexOf("Z4")==0){
			xzgj=100;
		}else if(cllx.indexOf("N")==0){
			xzgj=100;
			temp2=0.05f;
			temp1="±5%或±";
		}else if(cllx.indexOf("M")==0){
			xzgj=10;
		}
		Integer cz = vehCheckLogin.getZbzl()-(curbWeightData.getZbzl());
		
		Integer pd = Math.abs(cz)<xzgj?BaseDeviceData.PDJG_HG:BaseDeviceData.PDJG_BHG;
		
		Integer pd2 = Math.abs(cz*1.0/vehCheckLogin.getZbzl()*1.0)>temp2?BaseDeviceData.PDJG_BHG:BaseDeviceData.PDJG_HG;
		
		
		if(pd==BaseDeviceData.PDJG_HG||pd2==BaseDeviceData.PDJG_HG) {
			curbWeightData.setZbzlpd(BaseDeviceData.PDJG_HG);
		}else {
			curbWeightData.setZbzlpd(BaseDeviceData.PDJG_BHG);
		}

		return curbWeightData;

	}
	
	
	@Override
	public Integer startCheckQdz(VehCheckLogin vehCheckLogin) throws Exception, InterruptedException {
		
		deviceManyWeigh.sendMessage(ql);
		logger.info("清零返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
		// 开始新的一次检测
		createNew();
		Integer qz = check(vehCheckLogin, "驱动");
		return qz;
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
				try {
					deviceManyWeigh.sendMessage(dqsj);
					logger.info("读取称重数据："+dqsj);
					
					byte[] data = this.getDevData(new byte[19],A);
					
					
					logger.info("称重开始："+CharUtil.byte2HexOfString(data));
					
					Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
					Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
					
					boolean dw =false;
					
					if("前".equals(zw)) {
						dw=qzdw;
					}
					
					if("后".equals(zw)||"驱动".equals(zw)) {
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
				}catch (Exception e) {
					logger.error("称重异常！",e);
					this.getTemp().clear();
				}
			}
			
			String qtxx = this.deviceManyWeigh.getDevice().getQtxx();
			
			JSONObject qtxxjo = JSONObject.fromObject(qtxx);
			
			String sxtip = (String) qtxxjo.get("sxtip");
			String sxtdk = (String) qtxxjo.get("sxtdk");
			String sxtzh = (String) qtxxjo.get("sxtzh");
			String sxtmm = (String) qtxxjo.get("sxtmm");
			
			Map param1 =new HashMap();
			Map param2 =new HashMap();
			
			String[] ips =sxtip.split(",");
			param1.put("sxtip",ips[0]);
			param1.put("sxtdk",sxtdk);
			param1.put("sxtzh",sxtzh);
			param1.put("sxtmm",sxtmm);
			
			param2.put("sxtip",ips[1]);
			param2.put("sxtdk",sxtdk);
			param2.put("sxtzh",sxtzh);
			param2.put("sxtmm",sxtmm);
			
			if("前".equals(zw)) {
				TakePicture.custom(vehCheckLogin, "Z1", 0, "0362",param1);
				TakePicture.custom(vehCheckLogin, "Z1", 0, "0363",param2);
			}else if("后".equals(zw)) {
				TakePicture.custom(vehCheckLogin, "Z1", 0, "0363",param2);
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
		zcdw=false;
		
	}

	
	@Override
	public void init(DeviceManyWeigh deviceManyWeigh) {
		super.init(deviceManyWeigh);
	}


	

}
