package com.xs.veh.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.HBDeviceConfig;
import com.xs.veh.entity.HBRoutineCheck;
import com.xs.veh.manager.DeviceManager;
import com.xs.veh.manager.HBManager;
import com.xs.veh.network.data.JSGLData;

@Controller
@RequestMapping(value = "/hb")
@Modular(modelCode="hb",modelName="机动车环保检测")
public class HBController {
	

	@Autowired
	private HBManager hbManager;
	
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private DeviceManager deviceManager;
	
	@UserOperation(code="getHBDeviceConfig",name="查询机检测设备配置",isMain=false)
	@RequestMapping(value = "getHBDeviceConfig", method = RequestMethod.POST)
	public @ResponseBody List<HBDeviceConfig> getHBDeviceConfig(HBDeviceConfig param) {
		return hbManager.getHBDeviceConfigList(param);
	}
	
	@UserOperation(code="getHBDeviceConfigByIp",name="查询IP地址检测设备",isMain=false)
	@RequestMapping(value = "getHBDeviceConfigByIp", method = RequestMethod.POST)
	public @ResponseBody List<Device> getHBDeviceConfigByIp(HBDeviceConfig param) {
		param.setIp(getIpAddress());
		
		List<HBDeviceConfig>  datas = hbManager.getHBDeviceConfigList(param);
		List<Device> devices =new ArrayList<Device>();
		for(HBDeviceConfig dc:datas) {
			Device device = deviceManager.getDevice(dc.getDeviceId());
			if(device!=null) {
				devices.add(device);
			}
		}
		return devices;
	}
	
	
	@UserOperation(code="getRoutineCheck",name="日常检查",isMain=false)
	@RequestMapping(value = "getRoutineCheck", method = RequestMethod.POST)
	public @ResponseBody List<HBRoutineCheck> getRoutineCheck() {
		List<HBRoutineCheck>  datas = hbManager.getRoutineCheck();
		return datas;
	}
	
	
	@UserOperation(code="saveHBDeviceConfig",name="新增修改环保设备配置")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveHBDeviceConfig(HBDeviceConfig deviceConfig, BindingResult result) {
		deviceConfig = this.hbManager.saveHBDeviceConfig(deviceConfig);
		return ResultHandler.resultHandle(result, deviceConfig, "保存成功");
	}
	
	
	
	@UserOperation(code="saveJSGL",name="保存寄生功率")
	@RequestMapping(value = "saveJSGL", method = RequestMethod.POST)
	public @ResponseBody Map saveJSGL(@RequestBody List<JSGLData> datas, BindingResult result) {
		this.hbManager.saveJSGL(datas);
		return ResultHandler.resultHandle(result, datas, "保存成功");
	}
	
	
	 public String getIpAddress() {  
	        String ip = request.getHeader("x-forwarded-for");  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("Proxy-Client-IP");  
	        }  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("WL-Proxy-Client-IP");  
	        }  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("HTTP_CLIENT_IP");  
	        }  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
	        }  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getRemoteAddr();  
	        }  
	        return ip;  
	}
	 
	 
	 
	 
	 
}
