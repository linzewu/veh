package com.xs.veh.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.CoreFunction;
import com.xs.veh.manager.CoreFunctionManager;

@Controller
@RequestMapping(value = "/coreFunction",produces="application/json")
@Modular(modelCode="coreFunction",modelName="核心功能管理",isEmpowered=false)
public class CoreFunctionController {
	
	@Resource(name = "coreFunctionManager")
	private CoreFunctionManager coreFunctionManager;
	
	@Autowired
	private ServletContext servletContext;
	
	@UserOperation(code="save",name="保存核心功能")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveCoreFunction(@RequestParam("functionPoint") String functionPoint) {
			this.coreFunctionManager.deleteAllCoreFunction();
			String[] functionPo = functionPoint.split(",");
			List<CoreFunction> funs =new ArrayList<CoreFunction>();
			for(String str:functionPo) {
				CoreFunction core = new CoreFunction();
				core.setFunctionPoint(str);
				funs.add(core);
			}
			this.coreFunctionManager.save(funs);
			List<CoreFunction> coreList = this.coreFunctionManager.getAllCoreFunction();
			servletContext.setAttribute("coreFunctionList", coreList);
			return  ResultHandler.toSuccessJSON("保存核心功能成功！");		
	}
	
	@UserOperation(code="getAllCoreFunction",name="获取核心功能")
	@RequestMapping(value = "getAllCoreFunction", method = RequestMethod.POST)
	public @ResponseBody List<CoreFunction> getAllCoreFunction() {
		List<CoreFunction> list = (List<CoreFunction>)servletContext.getAttribute("coreFunctionList");
		return list;
	}

}
