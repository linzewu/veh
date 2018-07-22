package com.xs.veh.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.BlackList;
import com.xs.veh.entity.User;
import com.xs.veh.manager.BlackListManager;

@Controller
@RequestMapping(value = "/blackList",produces="application/json")
@Modular(modelCode="blackList",modelName="黑名单管理")
public class BlackListController {
	
	@Resource(name = "blackListManager")
	private BlackListManager blackListManager;
	
	@UserOperation(code="getBlackList",name="查询黑名单",isEmpowered=false)
	@RequestMapping(value = "getBlackList", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getBlackList(Integer page, Integer rows, BlackList black) {		
		List<BlackList> vcps = blackListManager.getList(page, rows, black);
		
		Integer total = blackListManager.getListCount(page, rows, black);
		
		Map<String,Object> data =new HashMap<String,Object>();
		
		data.put("rows", vcps);
		data.put("total", total);		
		
		return data;
	}
	
	@UserOperation(code="save",name="保存黑名单")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveBlackList(HttpSession session,@Valid BlackList blackList, BindingResult result) {
		if (!result.hasErrors()) {
			User user = (User)session.getAttribute("user");
			blackList.setCreateBy(user.getUserName());
			blackList.setLastUpdateTime(new Date());
			blackList.setFailCount(0);
			blackList.setEnableFlag("Y");
			this.blackListManager.saveBlackList(blackList);
			return  ResultHandler.resultHandle(result,null ,Constant.ConstantMessage.SAVE_SUCCESS);
		}else{
			return ResultHandler.resultHandle(result,null ,null);
		}
	}
	
	@UserOperation(code="delete",name="删除黑名单")
	@RequestMapping(value = "delete", method = RequestMethod.POST)
	public @ResponseBody void delete(BlackList blackList){
		this.blackListManager.deleteBlackList(blackList);
	}

}
