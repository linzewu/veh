package com.xs.veh.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.User;
import com.xs.veh.manager.DeviceManager;
import com.xs.veh.util.PageInfo;

@Controller
@RequestMapping(value = "/device")
public class DeviceController {
	
	@Resource(name = "deviceManager")
	private DeviceManager deviceManager;
	
	@RequestMapping(value = "getDevices", method = RequestMethod.POST)
	public @ResponseBody Map getUsers(User user, PageInfo pageInfo) {
		Map json = ResultHandler.toMyJSON(deviceManager.getDevices(),0);
		return json;
	}
	
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveUser(Device device, BindingResult result) {
		
		Device d = this.deviceManager.saveDevice(device);
			
		return  ResultHandler.resultHandle(result,d ,Constant.ConstantMessage.SAVE_SUCCESS);
		
	}
	
	@RequestMapping(value = "delete", method = RequestMethod.POST)
	public @ResponseBody void delete(Device device){
		this.deviceManager.deleteUser(device);
	}

	

}
