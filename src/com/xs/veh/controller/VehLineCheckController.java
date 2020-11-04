package com.xs.veh.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.common.Message;
import com.xs.common.ResultHandler;
import com.xs.veh.manager.VehLineCheckManager;

@Controller
@RequestMapping(value = "/linecheck")
public class VehLineCheckController {
	
	@Resource(name="vehLineCheckManager")
	private VehLineCheckManager vehLineCheckManager;
	
//	@RequestMapping(value = "upLine", method = RequestMethod.POST)
//	public @ResponseBody Map upLine(@RequestParam String jylsh,@RequestParam Integer jccs){
//		
//		Message message = vehLineCheckManager.upLine(jylsh, jccs);
//		
//		return ResultHandler.toMessage(message);
//	} 
//	
	

}
