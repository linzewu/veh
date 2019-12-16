package com.xs.veh.manager;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.xs.common.ResultHandler;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.DeviceMotion;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceManyWeigh;

@Service("deviceManager")
public class DeviceManager {

	@Value("${defaultDevice}")
	private String defaultDevice;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private ServletContext servletContext;


	public Integer getMaxLine() {

		List temp = this.hibernateTemplate.find("select max(jcxxh) from Device ");
		if (temp == null || temp.isEmpty()) {
			return null;
		}

		Integer maxLine = (Integer) temp.get(0);

		if (maxLine == null) {
			maxLine = 0;
		}

		return maxLine;
	}

	public void createLinkDevice() throws SystemException {
		Integer jcxxh = this.getMaxLine();
		if (jcxxh == null) {
			throw new SystemException(ResultHandler.toMyJSON(500, "获取检测线最大序号错误").toString());
		}
		jcxxh = jcxxh + 1;
		if (defaultDevice == null || defaultDevice.trim().equals("")) {
			throw new SystemException(ResultHandler.toMyJSON(500, "检测线默认设备模板未定义！").toString());
		}
		String[] devices = defaultDevice.split(",");
		for (String type : devices) {
			Device device = new Device();
			device.setType(Integer.parseInt(type));
			;
			device.setJcxxh(jcxxh);
			this.hibernateTemplate.save(device);
		}
	}

	public List<Device> getDevicesOfType() {
		List<Device> devices = (List<Device>) this.hibernateTemplate.find("From Device order by type desc");
		return devices;
	}

	public List<Device> getDevices() {

		List<Device> devices = (List<Device>) this.hibernateTemplate.find("From Device order by jcxxh asc");

		return devices;
	}

	public List<Device> getDevices(Integer jcxxh) {

		List<Device> devices = (List<Device>) this.hibernateTemplate
				.find("From Device where jcxxh=? and type<? order by jcxxh asc", jcxxh,90);

		return devices;
	}
	
	public List<Device> getDevicesByteType(Integer type) {

		List<Device> devices = (List<Device>) this.hibernateTemplate
				.find("From Device where type=? order by jcxxh asc", type);

		return devices;
	}
	
	public List<Device> getDevicesDisplay(Integer jcxxh) {
		List<Device> devices = (List<Device>) this.hibernateTemplate
				.find("From Device where jcxxh=? and type = ? ", jcxxh,91);
		return devices;
	}

	public Device getDevice(Integer id) {
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
	
	
	@Async
	public void asySetDefault(DeviceDisplay display) throws InterruptedException, IOException {
		Thread.sleep(3000);
		display.setDefault();
	}
	
	@Async
	public void upZ1(Integer deviceId,Integer vehCheckLoginId) throws InterruptedException, IOException {
		Device device=new Device();
		device.setId(deviceId);
		DeviceManyWeigh dmw = (DeviceManyWeigh)servletContext.getAttribute(device.getThredKey());
		VehCheckLogin vehCheckLogin =hibernateTemplate.load(VehCheckLogin.class, vehCheckLoginId);
		vehCheckLogin.setVehzbzlzt(VehCheckLogin.ZT_JCZ);
		dmw.startCheck(vehCheckLogin);
	}
	
}
