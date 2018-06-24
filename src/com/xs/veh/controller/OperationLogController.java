package com.xs.veh.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.veh.entity.OperationLog;
import com.xs.veh.manager.OperationLogManager;

@Controller
@RequestMapping(value = "/opeationLog")
@Modular(modelCode="OperationLog",modelName="日志管理")
public class OperationLogController {
	
	
	@Resource(name = "operationLogManager")
	private OperationLogManager operationLogManager;
	
	@UserOperation(code="getOperationLog",name="日志查询")
	@RequestMapping(value = "getOperationLog", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getOperationLog(Integer page, Integer rows, OperationLog operationLog) {

		
		List<OperationLog> vcps = operationLogManager.getOperationLog(page, rows, operationLog);
		
		Integer total = operationLogManager.getOperationLogCount(page, rows, operationLog);
		
		Map<String,Object> data =new HashMap<String,Object>();
		
		data.put("rows", vcps);
		data.put("total", total);
		
		
		return data;
	}

}
