package com.xs.veh.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.QueryObjectOutResponse;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.WriteObjectOutResponse;
import com.xs.veh.util.BeanXMLUtil;
import com.xs.veh.util.RCAConstant;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

@Service("vehManager")
public class VehManager {

	private static Logger logger = Logger.getLogger(VehManager.class);

	@Resource(name = "tmriJaxRpcOutAccessServiceStub")
	private TmriJaxRpcOutAccessServiceStub tro;
	
	
	
	
	@Value("#{rca[jkxlh]}")
	private String jkxlh;
	
	@Value("#{rca[jyjgbh]}")
	private String jyjgbh;
	
	

	private Document write(String jkid, Map data)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		TmriJaxRpcOutAccessServiceStub.WriteObjectOut woo = new TmriJaxRpcOutAccessServiceStub.WriteObjectOut();
		woo.setJkid(jkid);
		woo.setXtlb(RCAConstant.XTLB);
		woo.setJkxlh(jkxlh);
		Document xml = BeanXMLUtil.map2xml(data, "vehispara");
		woo.setUTF8XmlDoc(xml.asXML());
		WriteObjectOutResponse wor = tro.writeObjectOut(woo);
		String response = wor.getWriteObjectOutReturn();
		response = URLDecoder.decode(response, "utf-8");
		Document document = DocumentHelper.parseText(response);
		logger.info(document.asXML());
		return document;
	}

	private Document queryws(String jkid, Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		
		TmriJaxRpcOutAccessServiceStub.QueryObjectOut qoo = new TmriJaxRpcOutAccessServiceStub.QueryObjectOut();
		param.put("jyjgbh", jyjgbh);
		
		qoo.setJkid(jkid);
		qoo.setXtlb(RCAConstant.XTLB);
		qoo.setJkxlh(jkxlh);
		Document xml = BeanXMLUtil.map2xml(param, "QueryCondition");
		qoo.setUTF8XmlDoc(xml.asXML());
		QueryObjectOutResponse qoor = tro.queryObjectOut(qoo);

		String response = qoor.getQueryObjectOutReturn();
		response = URLDecoder.decode(response, "utf-8");
		Document document = DocumentHelper.parseText(response);
		return document;
	}

	public JSON getVehInfoOfws(Map param) throws RemoteException, UnsupportedEncodingException, DocumentException {

		Document document = this.queryws(RCAConstant.V18C49, param);
		
		return new  XMLSerializer().read(document.asXML());
	}
	
	
	public JSON getVehCheckItem(Map param) throws RemoteException, UnsupportedEncodingException, DocumentException{
		Document document = this.queryws(RCAConstant.V18C46, param);
		
		return new  XMLSerializer().read(document.asXML());
	}

}
