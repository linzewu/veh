package com.xs.veh.controller;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.Map;

import javax.annotation.Resource;

import org.dom4j.DocumentException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.veh.manager.VehManager;

import net.sf.json.JSON;

@Controller
@RequestMapping(value = "/veh")
public class VehController {

	@Resource(name = "vehManager")
	private VehManager vehManager;

	@RequestMapping(value = "getVehInfo", method = RequestMethod.POST)
	public @ResponseBody String getVehInfo(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		JSON json = vehManager.getVehInfoOfws(param);

		return json.toString();
	}
	
	@RequestMapping(value = "getVehCheckItem", method = RequestMethod.POST)
	public @ResponseBody String getVehCheckItem(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		JSON json = vehManager.getVehCheckItem(param);

		return json.toString();

	}

}
