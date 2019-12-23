package com.xs.veh.network.driver;

import java.io.IOException;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.AbstractDeviceVolume;
import com.xs.veh.network.data.VolumeData;

public class DeviceVolumeOfHY extends AbstractDeviceVolume {
	
	private String ks="43";
	
	private String qs="4D";
	
	private String fw="52";

	@Override
	public VolumeData startCheck(VehCheckLogin vc) throws IOException, InterruptedException {
		
		this.getTemp().clear();
		
		//发送复位
		this.deviceVolume.sendMessage(fw);
		
		//发送开始
		this.deviceVolume.sendMessage(ks);
		Thread.sleep(3*1000);
		//停止
		this.deviceVolume.sendMessage(ks);
		
		this.getTemp().clear();
		
		this.deviceVolume.sendMessage(qs);
		
		byte[] data= getDevData(new byte[9]);
		
		String  fb = new String(data);
		
		VolumeData volumeData =new VolumeData();
		volumeData.setFb(fb.trim());
		volumeData.setBaseDeviceData(vc, 0, "VL");
		
		return volumeData;
	}

}
