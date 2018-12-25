package com.xs.veh.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.Deserializers.Base;
import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.enums.CommonUserOperationEnum;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.manager.BaseParamsManager;

@Controller
@RequestMapping(value = "/bps")
@Modular(modelCode="BaseParams",modelName="系统参数",isEmpowered=false)
public class BaseParamsController {
	
	private static Logger logger = Logger.getLogger(BaseParamsController.class); 
	
	@Value("${jyjgmc}")
	private String jyjgmc;
	
	@Value("${sqrqz}")
	private String sqrqz;
	
	@Resource(name = "baseParamsManager")
	private BaseParamsManager baseParamsManager;
	
	@RequestMapping(value = "all.js")
	@UserOperation(code="all.js",name="数据字典",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	//@RequestMapping(value = "all.js", produces = {"text/javascript;charset=UTF-8"})
	public @ResponseBody String getBaseParamsOfJS(HttpServletRequest request)
			throws JsonProcessingException {
		
		ServletContext servletContext = request.getSession()
				.getServletContext();

		List<BaseParams> bps = (List<BaseParams>) servletContext
				.getAttribute("bps");

		ObjectMapper objectMapper = new 
				ObjectMapper();

		String js = " var bps=" + objectMapper.writeValueAsString(bps)+";\r\n";
		
		Map param=new HashMap();
		
		param.put("jyjgmc", jyjgmc);
		
		param.put("sqrqz", sqrqz);
		
		String vehComm= " var vehComm=" + objectMapper.writeValueAsString(param)+";\r\n";
		
		
		return js+vehComm;
	}

	@RequestMapping(value = "all.json")
	@UserOperation(code="all.js",name="数据字典",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	public @ResponseBody Map getBaseParams(HttpServletRequest request) {

		RequestContext requestContext = new RequestContext(request);

		ServletContext servletContext = request.getSession()
				.getServletContext();

		List<BaseParams> bps = (List<BaseParams>) servletContext
				.getAttribute("bps");

		return ResultHandler.toMyJSON(1,
				requestContext.getMessage(Constant.ConstantKey.SUCCESS), bps);
	}
	
	@RequestMapping(value = "save", method = RequestMethod.POST)
	@UserOperation(code="save",name="数据字典",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	public @ResponseBody Map save(BaseParams baseParams) {
		baseParams = this.baseParamsManager.save(baseParams);
		return ResultHandler.toMyJSON(Constant.ConstantState.STATE_SUCCESS, "保存成功",baseParams);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@UserOperation(code="delete",name="数据字典",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	public @ResponseBody Map delete(@RequestParam Integer id) {
		this.baseParamsManager.delete(id);
		return ResultHandler.toSuccessJSON("删除成功！");
	}

	@RequestMapping(value = "getBaseParamsOfPage")
	@UserOperation(code="getBaseParamsOfPage",name="数据字典",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	public @ResponseBody Map getBaseParamsOfPage(Integer page, Integer rows,BaseParams param) {
		Map data = this.baseParamsManager.getBaseParams(page,rows,param);
		return data;
	}
	
	@RequestMapping(value = "getBaseParamsByType")
	@UserOperation(code="getBaseParamsByType",name="根据类型查询数据字典",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	public @ResponseBody Map getBaseParamsByType(String type) {
		List<BaseParams> bpList = this.baseParamsManager.getBaseParamByType(type);
		return ResultHandler.toMyJSON(Constant.ConstantState.STATE_SUCCESS, "查询成功", bpList);
	}

}
