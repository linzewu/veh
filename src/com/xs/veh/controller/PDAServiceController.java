package com.xs.veh.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.common.Message;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.VehManager;

@Controller
@RequestMapping(value = "/pda")
public class PDAServiceController {
	
	@Resource(name="vehManager")
	private VehManager vehManager;
	
	@RequestMapping(value = "getCheckList", method = RequestMethod.POST)
	public @ResponseBody List getCheckList() {
		List<VehCheckLogin> data =  vehManager.getVehCheckLoginOfSXZT(VehCheckLogin.SXZT_WSX);
		return data;
	}
	
	@RequestMapping(value = "pushVehOnLine", method = RequestMethod.POST)
	public @ResponseBody Map pushVehOnLine(@RequestParam Integer id){
		
		Message message = this.vehManager.upLine(id);
		return ResultHandler.toMessage(message);
		
	}
 

}
