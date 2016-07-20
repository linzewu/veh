package com.xs.veh.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.network.data.BrakRollerData;

@Controller
@RequestMapping(value = "/report")
public class ReportController {

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@RequestMapping(value = "getReport1", method = RequestMethod.POST)
	public @ResponseBody Map getReport1(@RequestParam String jylsh) {
		Map data = checkDataManager.getReport1(jylsh);
		return data;
	}

	@RequestMapping(value = "getReport4", method = RequestMethod.POST)
	public @ResponseBody List getReport4(@RequestParam String jylsh) {
		List<BrakRollerData> array = checkDataManager.getReport4(jylsh);
		return array;
	}

	@RequestMapping(value = "getReport2", method = RequestMethod.POST)
	public @ResponseBody Map getReport2(@RequestParam String jylsh) {
		Map<String, List> data = checkDataManager.getReport2(jylsh);
		return data;
	}

}
