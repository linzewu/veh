package com.xs.veh.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.common.Message;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.ExternalCheckManager;
import com.xs.veh.manager.VehManager;

import net.sf.json.JSONArray;

@Controller
@RequestMapping(value = "/pda")
public class PDAServiceController {
	
	
	@Resource(name="vehManager")
	private VehManager vehManager;
	
	@Resource(name="externalCheckManager")
	private ExternalCheckManager externalCheckManager; 
	
	@RequestMapping(value = "getCheckList")
	public @ResponseBody String getCheckList(HttpServletRequest request,Integer status) {
		List<VehCheckLogin> data =  vehManager.getVehCheckLoginOfSXZT(status);
		String jsonp = ResultHandler.parserJSONP(request, JSONArray.fromObject(data).toString());
		
		return jsonp;
	}
	
	@RequestMapping(value = "pushVehOnLine")
	public @ResponseBody Map pushVehOnLine(@RequestParam Integer id){
		Message message = this.vehManager.upLine(id);
		return ResultHandler.toMessage(message);
	}
	
	
	@RequestMapping(value = "external", method = RequestMethod.POST)
	public @ResponseBody Map externalUpload(@RequestParam ExternalCheck externalCheck) {
		Message message = externalCheckManager.saveExternalCheck(externalCheck);
		return ResultHandler.toMessage(message);
	}
	
	@RequestMapping(value = "getExternal")
	public @ResponseBody List getExternal(String hphm) {
		List<VehCheckLogin> data   = externalCheckManager.getExternalCheckVhe(hphm);
		return data;
	}
 

}
