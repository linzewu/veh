package com.xs.veh.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.BaseParams;

@Controller
@RequestMapping(value = "/bps")
public class BaseParamsController {
	
	private static Logger logger = Logger.getLogger(BaseParamsController.class);  

	@RequestMapping(value = "all.js")
	//@RequestMapping(value = "all.js", produces = {"text/javascript;charset=UTF-8"})
	public @ResponseBody String getBaseParamsOfJS(HttpServletRequest request)
			throws JsonProcessingException {
		
		ServletContext servletContext = request.getSession()
				.getServletContext();

		List<BaseParams> bps = (List<BaseParams>) servletContext
				.getAttribute("bps");

		ObjectMapper objectMapper = new 
				ObjectMapper();

		String js = " var bps=" + objectMapper.writeValueAsString(bps);
		logger.debug(js);
		return js;
	}

	@RequestMapping(value = "all.json")
	public @ResponseBody Map getBaseParams(HttpServletRequest request) {

		RequestContext requestContext = new RequestContext(request);

		ServletContext servletContext = request.getSession()
				.getServletContext();

		List<BaseParams> bps = (List<BaseParams>) servletContext
				.getAttribute("bps");

		return ResultHandler.toMyJSON(1,
				requestContext.getMessage(Constant.ConstantKey.SUCCESS), bps);
	}

}
