package com.xs.veh.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.Map;

import javax.annotation.Resource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.rca.ws.client.TmriJaxRpcOutNewAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutNewAccessServiceStub.QueryObjectOutResponse;
import com.xs.veh.manager.BaseParamsManager;
import com.xs.veh.manager.VehManager;
import com.xs.veh.util.BeanXMLUtil;
import com.xs.veh.util.RCAConstant;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

@Controller
@RequestMapping(value = "/compulsoryInsurance")
public class CompulsoryInsuranceController {
	
	@Value("${jyjgbh}")
	private String jyjgbh;
	
	@Value("${jkxlh}")
	private String jkxlh;

	@Resource(name = "vehManager")
	private VehManager vehManager;
	
	@Resource(name = "baseParamsManager")
	private BaseParamsManager baseParamsManager;
	
	@Resource(name = "TmriJaxRpcOutNewAccessServiceStub")
	private TmriJaxRpcOutNewAccessServiceStub tro;
	
	@RequestMapping(value = "getVehTax", method = RequestMethod.POST)
	public @ResponseBody String getVehCheckItem(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		param.put("jyjgbh", jyjgbh);
		Document document = this.queryws(RCAConstant.V18C23, param);

		JSON json = new XMLSerializer().read(document.asXML());
		return json.toString();
	}
	
	public Document queryws(String jkid, Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		TmriJaxRpcOutNewAccessServiceStub.QueryObjectOut qoo = new TmriJaxRpcOutNewAccessServiceStub.QueryObjectOut();
		param.put("jyjgbh", jyjgbh);

		qoo.setJkid(jkid);
		qoo.setXtlb(RCAConstant.XTLB);
		qoo.setJkxlh(jkxlh);
		
		String bmdm = baseParamsManager.getBaseParam("jkcs", "bmdm").getParamName();
		String ywzdbm = baseParamsManager.getBaseParam("jkcs", "ywzdbm").getParamName();
		String jcip = baseParamsManager.getBaseParam("jkcs", "jcip").getParamName();
		qoo.setDwjgdm(bmdm);
		qoo.setDwmc(ywzdbm);
		qoo.setZdbs(jcip);
		
		Document xml = BeanXMLUtil.map2xml(param, "QueryCondition");
		qoo.setUTF8XmlDoc(xml.asXML());
		QueryObjectOutResponse qoor = tro.queryObjectOut(qoo);

		String response = qoor.getQueryObjectOutReturn();
		response = URLDecoder.decode(response, "utf-8");
		Document document = DocumentHelper.parseText(response);
		return document;
	}

}
