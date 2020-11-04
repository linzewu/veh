package com.xs.veh.network.driver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.AbstractDeviceManyWeigh;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceManyWeigh;
import com.xs.veh.network.TakePicture;
import com.xs.veh.network.data.BaseDeviceData;
import com.xs.veh.network.data.CurbWeightData;

import net.sf.json.JSONObject;

public class DeviceManyWeighDriverOfKLD20 extends AbstractDeviceManyWeigh {
	
	static Logger logger = Logger.getLogger(DeviceManyWeighDriverOfKLD20.class);

	
	public static final byte  A= CharUtil.hexStringToByte("41")[0];
	
	//零时重量
	private Integer lszl;

	@Override
	public void device2pc(byte[] ed) throws IOException {
		//super.device2pc(ed);
		if(ed[0]==0x02&&ed.length==12) {
			String temp=new String(new byte[] {ed[2],ed[3],ed[4],ed[5],ed[6],ed[7],ed[8]});
			lszl = Integer.valueOf(temp);
			lszl=lszl/10;
		}
		
	}


	@Override
	public CurbWeightData startCheck(VehCheckLogin vehCheckLogin) throws Exception, InterruptedException{
		

		String hphm = vehCheckLogin.getHphm();

		// 开始新的一次检测
		createNew();
		
		Integer zbzl = check(vehCheckLogin, "");
		
		CurbWeightData curbWeightData=new CurbWeightData();
		
		curbWeightData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), "Z1");
		
		curbWeightData.setZbzl((zbzl)-65);
		
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
		return 0;
	}
	
	
	private Integer check(VehCheckLogin vehCheckLogin,String zw) throws Exception, InterruptedException {
		Integer zbzl=0;
		try {
			// 显示屏显示信息
			this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
			this.display.sendMessage("整车称重请到位", DeviceDisplay.XP);
			int i = 0;
			while (true) {
				boolean dw =qzdw;
				if (dw) {
					this.display.sendMessage( "整车称重已到位", DeviceDisplay.SP);
					this.display.sendMessage( lszl + "KG",
							DeviceDisplay.XP);
					zbzl=lszl;
					i++;
				} else {
					this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
					this.display.sendMessage("整车称重请到位", DeviceDisplay.XP);
					i = 0;
				}

				if (i >= 12) {
					break;
				}

				Thread.sleep(500);
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
			
			TakePicture.custom(vehCheckLogin, "Z1", 0, "0362",param1);
			TakePicture.custom(vehCheckLogin, "Z1", 0, "0363",param2);
			
			logger.info("整车称重结束");
			this.display.sendMessage("整车称重结束", DeviceDisplay.SP);
			this.display.sendMessage((zbzl) + "KG", DeviceDisplay.XP);
			
			return zbzl;
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
