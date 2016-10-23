package com.xs.veh.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.common.Constant;
import com.xs.common.Message;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.WorkPoint;
import com.xs.veh.manager.WorkPointManager;

@Controller
@RequestMapping(value = "/workpoint")
public class WorkPointController {

	@Resource(name = "workPointManager")
	private WorkPointManager workPointManager;
	
	@RequestMapping(value = "getWorkPoints", method = RequestMethod.POST)
	public @ResponseBody Map getWorkPoints() {
		List<WorkPoint> wps = this.workPointManager.getWorkPoints();
		return ResultHandler.toMyJSON(wps, wps.size());
	}

	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveWorkPoint(WorkPoint workPoint, BindingResult result) {
		
		//如果为空则不添加
		if(workPoint.getGwzt()==null||"".equals(workPoint.getGwzt())){
			workPoint.setGwzt(0);
		}
		WorkPoint wp = this.workPointManager.saveWorkPoint(workPoint);
		return ResultHandler.resultHandle(result, wp, Constant.ConstantMessage.SAVE_SUCCESS);
	}
	
	@RequestMapping(value = "start", method = RequestMethod.POST)
	public @ResponseBody Map start(@RequestParam Integer id) {
		Message message = workPointManager.startWorkpoint(id);
		return ResultHandler.toMessage(message);
	}
	
	@RequestMapping(value = "stop", method = RequestMethod.POST)
	public @ResponseBody Map stop(@RequestParam Integer id) throws InterruptedException {
		Message message = workPointManager.stopWorkpoint(id);
		return ResultHandler.toMessage(message);
	}
	
	@RequestMapping(value = "reStart", method = RequestMethod.POST)
	public @ResponseBody Map reStart(@RequestParam Integer id) throws InterruptedException {
		Message message = workPointManager.reStartWorkpoint(id);
		return ResultHandler.toMessage(message);
	}
	
	
	

}
