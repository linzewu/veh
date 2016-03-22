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
import com.xs.veh.entity.Switch8;
import com.xs.veh.entity.User;
import com.xs.veh.manager.DeviceManager;
import com.xs.veh.util.PageInfo;

@Controller
@RequestMapping(value = "/switch")
public class SwitchController {
	
	@Resource(name = "deviceManager")
	private DeviceManager deviceManager;
	
	@RequestMapping(value = "getSwitchs", method = RequestMethod.POST)
	public @ResponseBody Map getSwitchs(User user, PageInfo pageInfo) {
		Map json = ResultHandler.toMyJSON(deviceManager.getSwitchs(),0);
		return json;
	}
	
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveSwitch(Switch8 switch8, BindingResult result) {
		
		Switch8 s = this.deviceManager.saveSwitch8(switch8);
			
		return  ResultHandler.resultHandle(result,s ,Constant.ConstantMessage.SAVE_SUCCESS);
		
	}
	
	@RequestMapping(value = "delete", method = RequestMethod.POST)
	public @ResponseBody void delete(Switch8 switch8){
		this.deviceManager.deleteSwitch8(switch8);
	}

	

}
