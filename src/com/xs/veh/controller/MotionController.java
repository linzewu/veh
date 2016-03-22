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
import com.xs.veh.entity.DeviceMotion;
import com.xs.veh.entity.User;
import com.xs.veh.manager.DeviceManager;
import com.xs.veh.util.PageInfo;

@Controller
@RequestMapping(value = "/motion")
public class MotionController {
	
	@Resource(name = "deviceManager")
	private DeviceManager deviceManager;
	
	@RequestMapping(value = "getMotions", method = RequestMethod.POST)
	public @ResponseBody Map getUsers(User user, PageInfo pageInfo) {
		Map json = ResultHandler.toMyJSON(deviceManager.getMotions(),0);
		return json;
	}
	
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveUser(DeviceMotion deviceMotion, BindingResult result) {
		
		DeviceMotion dm = this.deviceManager.saveDeviceMotion(deviceMotion);
			
		return  ResultHandler.resultHandle(result,dm ,Constant.ConstantMessage.SAVE_SUCCESS);
		
	}
	
	@RequestMapping(value = "delete", method = RequestMethod.POST)
	public @ResponseBody void delete(DeviceMotion deviceMotion){
		this.deviceManager.deleteDeviceMotion(deviceMotion);
	}

	

}
