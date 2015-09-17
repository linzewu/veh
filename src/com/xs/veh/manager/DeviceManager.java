package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.Device;
import com.xs.veh.entity.DeviceMotion;

@Service("deviceManager")
public class DeviceManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public List<Device> getDevices() {

		List<Device> devices = (List<Device>) this.hibernateTemplate.find("From Device");

		return devices;
	}

	public Device saveDevice(Device device) {

		return this.hibernateTemplate.merge(device);

	}

	public void deleteUser(Device device) {

		this.hibernateTemplate.delete(device);
	}
	
	
	public List<DeviceMotion> getMotions(){
		
		List<DeviceMotion> devices = (List<DeviceMotion>) this.hibernateTemplate.find("From DeviceMotion");

		return devices;
	}
	
	public DeviceMotion saveDeviceMotion(DeviceMotion deviceMotion) {

		return this.hibernateTemplate.merge(deviceMotion);

	}

	public void deleteDeviceMotion(DeviceMotion deviceMotion) {

		this.hibernateTemplate.delete(deviceMotion);
	}

}
