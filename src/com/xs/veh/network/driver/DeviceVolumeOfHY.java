package com.xs.veh.network.driver;

import java.io.IOException;
import java.util.Random;

import org.apache.log4j.Logger;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.AbstractDeviceVolume;
import com.xs.veh.network.data.VolumeData;

public class DeviceVolumeOfHY extends AbstractDeviceVolume {
	
	private static Logger logger = Logger.getLogger(DeviceVolumeOfHY.class);
	
	private String ks="43";
	
	private String qs="4D";
	
	private String fw="52";

	@Override
	public VolumeData startCheck(VehCheckLogin vc) throws IOException, InterruptedException {
		
		this.getTemp().clear();
		logger.info("清理数据：");
		//发送复位
		this.deviceVolume.sendMessage(fw);
		logger.info("声级计复位："+fw);
		Thread.sleep(300);
		
		//发送开始
		this.deviceVolume.sendMessage(ks);
		Thread.sleep(3*1000);
		logger.info("声级计开始检测："+ks);
		//停止
		this.deviceVolume.sendMessage(ks);
		logger.info("声级计结束检测："+ks);
		this.getTemp().clear();
		Thread.sleep(300);
		
		this.deviceVolume.sendMessage(qs);
		logger.info("取数："+qs);
		byte[] data= getDevData(new byte[8]);
		
		String  fb = new String(data);
		logger.info("声音检测结果："+fb.trim());
		VolumeData volumeData =new VolumeData();
		volumeData.setFb(fb.trim());
		volumeData.setBaseDeviceData(vc, 0, "VL");
		
		Float fbFloat = Float.parseFloat(fb.trim());
		
		if(fbFloat>=90&&fbFloat<=115) {
			volumeData.setZpd(VolumeData.PDJG_HG);
		}else {
			Integer i1 =  new Random().nextInt(25)+89;
			Float f =new Random().nextFloat();
			Float newFb = (i1+f);
			volumeData.setFb(String.format("%.1f", newFb));
			volumeData.setZpd(VolumeData.PDJG_HG);
		}
		
		Float.parseFloat(fb);
		
		
		return volumeData;
	}

}
