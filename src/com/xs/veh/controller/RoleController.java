package com.xs.veh.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.OperationLog;
import com.xs.veh.entity.PowerPoint;
import com.xs.veh.entity.Role;
import com.xs.veh.entity.User;
import com.xs.veh.manager.OperationLogManager;
import com.xs.veh.manager.RoleManager;

@Controller
@RequestMapping(value = "/role",produces="application/json")
@Modular(modelCode="role",modelName="角色管理")
public class RoleController {
	
	@Autowired
	private ServletContext servletContext;
	
	@Resource(name = "roleManager")
	private RoleManager roleManager;
	
	@UserOperation(code="getPowerPoints",name="获取权限列表")
	@RequestMapping(value = "getPowerPoints", method = RequestMethod.POST)
	public @ResponseBody List<PowerPoint> getPowerPoints() {
		List<PowerPoint> powerPoints = (List<PowerPoint>) servletContext.getAttribute("powerPoints");
		return powerPoints;
	}
	
	@UserOperation(code="getRoles",name="查询角色")
	@RequestMapping(value = "getRoles", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getRoles(Integer page, Integer rows, Role role) {	
		List<Role> vcps = roleManager.getRole(page, rows, role);
		
		Integer total = roleManager.getRoleCount(page, rows, role);
		
		Map<String,Object> data =new HashMap<String,Object>();
		
		data.put("rows", vcps);
		data.put("total", total);		
		return data;
	}
	@UserOperation(code="delete",name="删除角色")
	@RequestMapping(value = "delete", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> delete(@RequestParam Integer id) throws Exception {
		roleManager.delete(id);
		return ResultHandler.toSuccessJSON("角色删除成功");
	}
	
	@UserOperation(code="save",name="保存角色")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveRole(@Valid Role role, BindingResult result) {
		if (!result.hasErrors()) {
			Role r = this.roleManager.saveRole(role);
			return  ResultHandler.resultHandle(result,r ,Constant.ConstantMessage.SAVE_SUCCESS);
		}else{
			return ResultHandler.resultHandle(result,null ,null);
		}
	}
	
	@UserOperation(code="validateRoleName",name="校验角色名",isMain=false)
	@RequestMapping(value = "validateRoleName")
	public @ResponseBody boolean validateRoleName(Role role) {
		Role querRole = this.roleManager.queryRoleByRoleName(role);
		if(querRole==null){
			return true;
		}else{
			return false;
		}

	}
	

}
