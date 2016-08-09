package com.xs.veh.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lzw.security.util.WindowsInfoUtil;
import com.xs.common.BaseParamsUtil;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.SystemInfo;
import com.xs.veh.manager.BaseParamsManager;

@Controller
@RequestMapping(value = "/sys")
public class SystemController {

	@Resource(name = "systemInfo")
	private SystemInfo systemInfo;
	
	@Resource(name = "baseParamsManager")
	private BaseParamsManager baseParamsManager;
	
	@Autowired
	private ServletContext servletContext;

	public static final String SETTING = "配置信息";

	public static final String SYSTEM = "系统信息";

	@RequestMapping(value = "getInfo", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> getSystemInfo(@RequestParam Map param) {

		String jyjgmc = "";
		List<BaseParams> bps = BaseParamsUtil.getBaseParamsByType("jyjgmc");
		if (bps.size() > 0) {
			jyjgmc = bps.get(0).getParamName();
		}

		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();

		Map<String, String> sm1 = new HashMap<String, String>();
		// 加載配置信息
		sm1.put("name", "检验机构编号");
		sm1.put("value", systemInfo.getJyjgbh());
		sm1.put("group", SETTING);
		rows.add(sm1);

		Map<String, String> sm2 = new HashMap<String, String>();
		sm2.put("name", "检验机构名称");
		sm2.put("value", jyjgmc);
		sm2.put("group", SETTING);
		rows.add(sm2);

		Map<String, String> sm3 = new HashMap<String, String>();
		sm3.put("name", "监管平台IP");
		sm3.put("value", systemInfo.getJgxtip());
		sm3.put("group", SETTING);
		rows.add(sm3);

		Map<String, String> sm4 = new HashMap<String, String>();
		sm4.put("name", "监管平台端口");
		sm4.put("value", systemInfo.getJgxtdk());
		sm4.put("group", SETTING);
		rows.add(sm4);

		Map<String, String> sm5 = new HashMap<String, String>();
		sm5.put("name", "接口序列号");
		sm5.put("value", systemInfo.getJkxlh());
		sm5.put("group", SETTING);
		rows.add(sm5);

		Map<String, String> sm6 = new HashMap<String, String>();
		sm6.put("name", "数据库连接信息");
		sm6.put("value", systemInfo.getDbInfo());
		sm6.put("group", SETTING);
		rows.add(sm6);

		getComputerInfo(rows);

		Map<String, Object> rm = new HashMap<String, Object>();
		rm.put("rows", rows);

		return rm;
	}

	private void getComputerInfo(List<Map<String, String>> rows) {

		Properties p = System.getProperties();

		Map<String, String> sm1 = new HashMap<String, String>();
		sm1.put("name", "系统名称");
		sm1.put("value", p.getProperty("os.name"));
		sm1.put("group", SYSTEM);
		rows.add(sm1);

		Map<String, String> sm2 = new HashMap<String, String>();
		sm2.put("name", "系统架构");
		sm2.put("value", p.getProperty("os.arch"));
		sm2.put("group", SYSTEM);
		rows.add(sm2);

		Map<String, String> sm3 = new HashMap<String, String>();
		sm3.put("name", "系统版本");
		sm3.put("value", p.getProperty("os.version"));
		sm3.put("group", SYSTEM);
		rows.add(sm3);

		Map<String, String> sm4 = new HashMap<String, String>();
		sm4.put("name", "CPU个数");
		sm4.put("value", String.valueOf(Runtime.getRuntime().availableProcessors()));
		sm4.put("group", SYSTEM);
		rows.add(sm4);

		Map<String, String> sm5 = new HashMap<String, String>();
		sm5.put("name", "虚拟机内存");
		sm5.put("value", "总量：" + String.valueOf(Runtime.getRuntime().totalMemory()) + " -- 空闲："
				+ String.valueOf(Runtime.getRuntime().freeMemory()) + " -- 使用最大内存量：" + Runtime.getRuntime().maxMemory());
		sm5.put("group", SYSTEM);
		
		Map<String, String> sm6 = new HashMap<String, String>();
		sm6.put("name", "磁盤使用情況");
		sm6.put("value", WindowsInfoUtil.getDisk().toString());
		sm6.put("group", SYSTEM);
		
		rows.add(sm6);

	}
	
	@RequestMapping(value = "sysParamReload", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> sysParamReload() {
		List<BaseParams> bps = baseParamsManager.getBaseParams();
		servletContext.setAttribute("bps", bps);
		return ResultHandler.toSuccessJSON("系统参数刷新成功");
	}

}
