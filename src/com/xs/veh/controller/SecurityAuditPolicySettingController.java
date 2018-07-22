package com.xs.veh.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.SecurityAuditPolicySetting;
import com.xs.veh.manager.SecurityAuditPolicySettingManager;

@Controller
@RequestMapping(value = "/securityAuditPolicySetting")
@Modular(modelCode="securityAuditPolicySetting",modelName="安全策略",isEmpowered=false)
public class SecurityAuditPolicySettingController {
	
	@Resource(name = "securityAuditPolicySettingManager")
	private SecurityAuditPolicySettingManager securityAuditPolicySettingManager;
	
	@UserOperation(code="getPolicySettingList",name="查询安全策略")
	@RequestMapping(value = "getPolicySettingList", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getPolicySettingList(Integer page, Integer rows, SecurityAuditPolicySetting securityAuditPolicySetting) {		
		List<SecurityAuditPolicySetting> vcps = securityAuditPolicySettingManager.getList(page, rows, securityAuditPolicySetting);
		
		Integer total = securityAuditPolicySettingManager.getListCount(page, rows, securityAuditPolicySetting);
		
		Map<String,Object> data =new HashMap<String,Object>();
		
		data.put("rows", vcps);
		data.put("total", total);		
		
		return data;
	}
	
	@UserOperation(code="save",name="编辑安全策略")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map save(@RequestBody SecurityAuditPolicySetting svo, BindingResult result) {
		
		if (!result.hasErrors()) {
			this.securityAuditPolicySettingManager.updateSecurityAuditPolicySetting(svo.getUpdateList());
			return  ResultHandler.resultHandle(result,null ,Constant.ConstantMessage.SAVE_SUCCESS);
		}else{
			return ResultHandler.resultHandle(result,null ,null);
		}
	}

}
