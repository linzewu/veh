package com.xs.veh.controller;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.Map;

import javax.annotation.Resource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.veh.manager.VehManager;
import com.xs.veh.util.RCAConstant;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

@Controller
@RequestMapping(value = "/compulsoryInsurance")
public class CompulsoryInsuranceController {
	
	@Value("${jyjgbh}")
	private String jyjgbh;

	@Resource(name = "vehManager")
	private VehManager vehManager;
	
	@RequestMapping(value = "getVehTax", method = RequestMethod.POST)
	public @ResponseBody String getVehCheckItem(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		param.put("jyjgbh", jyjgbh);
		Document document = vehManager.queryws(RCAConstant.V18C23, param);

		JSON json = new XMLSerializer().read(document.asXML());
		return json.toString();
	}

}
