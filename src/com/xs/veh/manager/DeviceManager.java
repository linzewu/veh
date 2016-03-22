package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.common.ResultHandler;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.DeviceMotion;
import com.xs.veh.entity.Switch8;

@Service("deviceManager")
public class DeviceManager {

	@Value("${defaultDevice}")
	private String defaultDevice;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public Integer getMaxLine() {

		List temp = this.hibernateTemplate.find("select max(jcxxh) from Device ");
		if (temp == null || temp.isEmpty()) {
			return null;
		}
		
		
		Integer maxLine = (Integer) temp.get(0);
		
		if(maxLine==null){
			maxLine=0;
		}
		
		return maxLine;
	}

	public void createLinkDevice() throws SystemException{
		Integer jcxxh = this.getMaxLine();
		if(jcxxh==null){
			throw new SystemException(ResultHandler.toMyJSON(500, "获取检测线最大序号错误").toString());
		}
		jcxxh=jcxxh+1;
		if(defaultDevice==null||defaultDevice.trim().equals("")){
			throw new SystemException(ResultHandler.toMyJSON(500, "检测线默认设备模板未定义！").toString());
		}
		String[] devices=defaultDevice.split(",");
		for(String type:devices){
			Device device=new Device();
			device.setType(Integer.parseInt(type));;
			device.setJcxxh(jcxxh);
			this.hibernateTemplate.save(device);
		}
	}
	
	public List<Device> getDevicesOfType(){
		List<Device> devices = (List<Device>) this.hibernateTemplate.find("From Device order by type desc");
		return devices;
	}

	public List<Device> getDevices() {

		List<Device> devices = (List<Device>) this.hibernateTemplate.find("From Device order by jcxxh asc");

		return devices;
	}
	
	public Device getDevice(Integer id){
		
		return this.hibernateTemplate.get(Device.class, id);
		
	}

	public Device saveDevice(Device device) {

		return this.hibernateTemplate.merge(device);

	}

	public void deleteDevice(Device device) {

		this.hibernateTemplate.delete(device);
	}

	public List<DeviceMotion> getMotions() {

		List<DeviceMotion> devices = (List<DeviceMotion>) this.hibernateTemplate.find("From DeviceMotion");

		return devices;
	}

	public DeviceMotion saveDeviceMotion(DeviceMotion deviceMotion) {

		return this.hibernateTemplate.merge(deviceMotion);

	}

	public void deleteDeviceMotion(DeviceMotion deviceMotion) {

		this.hibernateTemplate.delete(deviceMotion);
	}

	public List<Switch8> getSwitchs() {

		List<Switch8> switchs = (List<Switch8>) this.hibernateTemplate.find("From Switch8");

		return switchs;
	}

	public Switch8 saveSwitch8(Switch8 switch8) {

		return this.hibernateTemplate.merge(switch8);

	}

	public void deleteSwitch8(Switch8 switch8) {

		this.hibernateTemplate.delete(switch8);
	}

}
