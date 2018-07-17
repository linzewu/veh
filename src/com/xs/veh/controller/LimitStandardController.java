package com.xs.veh.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.enums.CommonUserOperationEnum;
import com.xs.veh.entity.LimitStandard;
import com.xs.veh.manager.LimitStandardManager;
@Controller
@RequestMapping(value = "/limitStandard",produces="application/json")
@Modular(modelCode="limitStandard",modelName="检测项目和标准限值")
public class LimitStandardController {
	
	@Resource(name = "limitStandardManager")
	private LimitStandardManager limitStandardManager;
	
	@UserOperation(code="getAllLimitStandard",name="获取所有检测项目和标准限值",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	@RequestMapping(value = "getAllLimitStandard", method = RequestMethod.POST)
	public @ResponseBody List<LimitStandard> getAllLimitStandard() {
		return limitStandardManager.getAllLimitStandard();
	}

}
